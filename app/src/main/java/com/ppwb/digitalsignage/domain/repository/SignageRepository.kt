package com.ppwb.digitalsignage.domain.repository

import com.ppwb.digitalsignage.domain.model.SignageData
import kotlinx.coroutines.flow.Flow

interface SignageRepository {
    fun getSignageData(): Flow<SignageData?>
    suspend fun syncData(serialNumber: String, currentVersion: String?): Result<SignageData?>
    suspend fun saveSignageData(data: SignageData)
    suspend fun prepareAssets(assets: List<com.ppwb.digitalsignage.domain.model.Asset>)
    suspend fun updateAssetLocalPath(assetId: String, path: String)
    suspend fun getAssetsToDownload(): List<com.ppwb.digitalsignage.domain.model.Asset>
    suspend fun savePendingSync(version: String, json: String)
    suspend fun applyPendingSync()
}
