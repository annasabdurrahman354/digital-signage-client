package com.ppwb.digitalsignage.data.local.dao

import androidx.room.*
import com.ppwb.digitalsignage.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SignageDao {
    @Query("SELECT version FROM config LIMIT 1")
    suspend fun getCurrentVersion(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Query("SELECT * FROM device LIMIT 1")
    fun getDevice(): Flow<DeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Query("DELETE FROM schedules")
    suspend fun clearSchedules()

    @Query("SELECT * FROM schedules")
    fun getSchedules(): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>)

    @Query("DELETE FROM playlists")
    suspend fun clearPlaylists()

    @Query("SELECT * FROM playlists")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItems(items: List<PlaylistItemEntity>)

    @Query("DELETE FROM playlist_items")
    suspend fun clearPlaylistItems()

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun getItemsForPlaylist(playlistId: String): List<PlaylistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Query("SELECT * FROM assets")
    fun getAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: String): AssetEntity?

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSync(pendingSync: PendingSyncEntity)

    @Query("SELECT * FROM pending_sync LIMIT 1")
    suspend fun getPendingSync(): PendingSyncEntity?

    @Query("DELETE FROM pending_sync")
    suspend fun clearPendingSync()

    @Transaction
    suspend fun updateAllData(
        version: String,
        device: DeviceEntity,
        schedules: List<ScheduleEntity>,
        playlists: List<PlaylistEntity>,
        playlistItems: List<PlaylistItemEntity>,
        assets: List<AssetEntity>
    ) {
        insertConfig(ConfigEntity(version = version))
        insertDevice(device)
        clearSchedules()
        insertSchedules(schedules)
        clearPlaylists()
        insertPlaylists(playlists)
        clearPlaylistItems()
        insertPlaylistItems(playlistItems)
        // Note: Assets are handled carefully to not delete existing downloaded files
        // We might want to only insert new assets or update existing ones
        insertAssets(assets)
    }
}
