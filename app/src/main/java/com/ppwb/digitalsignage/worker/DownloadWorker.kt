package com.ppwb.digitalsignage.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ppwb.digitalsignage.domain.repository.SignageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SignageRepository
) : CoroutineWorker(appContext, workerParams) {

    private val client = OkHttpClient()

    override suspend fun doWork(): Result {
        val assetsToDownload = repository.getAssetsToDownload()

        var allSuccess = true
        for (asset in assetsToDownload) {
            val success = downloadAsset(asset)
            if (!success) allSuccess = false
        }

        return finalizeWork(allSuccess)
    }

    private suspend fun downloadAsset(asset: com.ppwb.digitalsignage.domain.model.Asset): Boolean {
        // Run blocking network and file I/O operations on the IO dispatcher
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(asset.fileUrl).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val fileName = asset.fileUrl.substringAfterLast("/")
                    val file = File(applicationContext.filesDir, fileName)

                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }

                    repository.updateAssetLocalPath(asset.id, file.absolutePath)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    private suspend fun finalizeWork(allSuccess: Boolean): Result {
        if (allSuccess) {
            repository.applyPendingSync()
        }
        return if (allSuccess) Result.success() else Result.retry()
    }
}