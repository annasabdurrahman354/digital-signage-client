package com.ppwb.digitalsignage.data.repository

import com.ppwb.digitalsignage.data.local.dao.SignageDao
import com.ppwb.digitalsignage.data.local.entity.*
import com.ppwb.digitalsignage.data.remote.SignageApi
import com.ppwb.digitalsignage.domain.model.*
import com.ppwb.digitalsignage.domain.repository.SignageRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class SignageRepositoryImpl @Inject constructor(
    private val api: SignageApi,
    private val dao: SignageDao
) : SignageRepository {

    override fun getSignageData(): Flow<SignageData?> {
        return combine(
            dao.getDevice(),
            dao.getSchedules(),
            dao.getPlaylists(),
            dao.getAssets()
        ) { device, schedules, playlists, assets ->
            if (device == null) return@combine null

            val mappedPlaylists = playlists.map { p ->
                Playlist(
                    id = p.id,
                    name = p.name,
                    items = dao.getItemsForPlaylist(p.id).map { item ->
                        PlaylistItem(item.assetId, item.orderIndex, item.durationSec)
                    }
                )
            }

            SignageData(
                version = dao.getCurrentVersion() ?: "",
                device = Device(device.id, device.deviceName, device.serialNumber, device.location, device.defaultPlaylistId),
                schedules = schedules.map { s ->
                    Schedule(s.id, s.playlistId, s.priority, s.validFromDate, s.validUntilDate, s.timeStart, s.timeEnd, s.daysOfWeek.split(",").map { it.toInt() })
                },
                playlists = mappedPlaylists,
                assets = assets.map { a ->
                    Asset(a.id, a.name, a.type, a.fileUrl, a.hashMd5, a.sizeBytes, a.localPath)
                }
            )
        }
    }

    override suspend fun syncData(serialNumber: String, currentVersion: String?): Result<SignageData?> {
        return try {
            val response = api.sync(serialNumber, currentVersion)
            if (response.code() == 304) {
                Result.success(null)
            } else if (response.isSuccessful) {
                val body = response.body()!!
                // Store the raw JSON for later application
                val json = com.google.gson.Gson().toJson(body)
                savePendingSync(body.version, json)
                Result.success(mapResponseToDomain(body))
            } else {
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSignageData(data: SignageData) {
        val deviceEntity = DeviceEntity(data.device.id, data.device.deviceName, data.device.serialNumber, data.device.location, data.device.defaultPlaylistId)
        val scheduleEntities = data.schedules.map { s ->
            ScheduleEntity(s.id, s.playlistId, s.priority, s.validFromDate, s.validUntilDate, s.timeStart, s.timeEnd, s.daysOfWeek.joinToString(","))
        }
        val playlistEntities = data.playlists.map { p -> PlaylistEntity(p.id, p.name) }
        val playlistItemEntities = data.playlists.flatMap { p ->
            p.items.map { i -> PlaylistItemEntity(playlistId = p.id, assetId = i.assetId, orderIndex = i.order, durationSec = i.durationSec) }
        }
        val assetEntities = data.assets.map { a ->
            val existing = dao.getAssetById(a.id)
            AssetEntity(a.id, a.name, a.type, a.fileUrl, a.hashMd5, a.sizeBytes, existing?.localPath)
        }

        dao.updateAllData(data.version, deviceEntity, scheduleEntities, playlistEntities, playlistItemEntities, assetEntities)
    }

    override suspend fun prepareAssets(assets: List<Asset>) {
        val assetEntities = assets.map { a ->
            val existing = dao.getAssetById(a.id)
            AssetEntity(a.id, a.name, a.type, a.fileUrl, a.hashMd5, a.sizeBytes, existing?.localPath)
        }
        dao.insertAssets(assetEntities)
    }

    override suspend fun updateAssetLocalPath(assetId: String, path: String) {
        val asset = dao.getAssetById(assetId)
        if (asset != null) {
            dao.updateAsset(asset.copy(localPath = path))
        }
    }

    override suspend fun getAssetsToDownload(): List<Asset> {
        return dao.getAssets().first().filter { it.localPath == null }.map { a ->
            Asset(a.id, a.name, a.type, a.fileUrl, a.hashMd5, a.sizeBytes, a.localPath)
        }
    }

    override suspend fun savePendingSync(version: String, json: String) {
        dao.insertPendingSync(PendingSyncEntity(version, json))
    }

    override suspend fun applyPendingSync() {
        val pending = dao.getPendingSync() ?: return
        val response = com.google.gson.Gson().fromJson(pending.jsonData, com.ppwb.digitalsignage.data.remote.dto.SignageResponse::class.java)
        val data = mapResponseToDomain(response)
        
        // Check if all assets for this new data are downloaded
        val assetIds = data.assets.map { it.id }
        var allDownloaded = true
        for (id in assetIds) {
            if (dao.getAssetById(id)?.localPath == null) {
                allDownloaded = false
                break
            }
        }
        
        if (allDownloaded) {
            saveSignageData(data)
            dao.clearPendingSync()
        }
    }

    private fun mapResponseToDomain(response: com.ppwb.digitalsignage.data.remote.dto.SignageResponse): SignageData {
        return SignageData(
            version = response.version,
            device = Device(response.device.id, response.device.deviceName, response.device.serialNumber, response.device.location, response.device.defaultPlaylistId),
            schedules = response.schedules.map { s ->
                val days = s.daysOfWeek.map { 
                    when(it) {
                        is Double -> it.toInt()
                        is String -> it.toInt()
                        else -> 0
                    }
                }
                Schedule(s.id, s.playlistId, s.priority, s.validFromDate, s.validUntilDate, s.timeStart, s.timeEnd, days)
            },
            playlists = response.playlists.map { p ->
                Playlist(p.id, p.name, p.items.map { i -> PlaylistItem(i.assetId, i.order, i.durationSec) })
            },
            assets = response.assets.map { a ->
                Asset(a.id, a.name, a.type, a.fileUrl, a.hashMd5, a.sizeBytes)
            }
        )
    }
}
