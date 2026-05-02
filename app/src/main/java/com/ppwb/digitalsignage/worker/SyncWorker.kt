package com.ppwb.digitalsignage.worker

import android.content.Context
import android.provider.Settings
import androidx.hilt.work.HiltWorker
import androidx.work.*
import androidx.work.NetworkType
import com.ppwb.digitalsignage.data.local.dao.SignageDao
import com.ppwb.digitalsignage.domain.repository.SignageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SignageRepository,
    private val dao: SignageDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val androidId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        val currentVersion = dao.getCurrentVersion()

        val result = repository.syncData(androidId, currentVersion)
        
        return if (result.isSuccess) {
            val newData = result.getOrNull()
            if (newData != null) {
                // Prepare assets metadata
                repository.prepareAssets(newData.assets)
                
                // Trigger DownloadWorker
                val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>().build()
                WorkManager.getInstance(applicationContext).enqueue(downloadWork)
            }
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        fun startPeriodicSync(context: Context) {
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(10, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "SignageSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
