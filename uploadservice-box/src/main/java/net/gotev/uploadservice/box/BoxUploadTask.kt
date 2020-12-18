package net.gotev.uploadservice.box

import com.box.androidsdk.content.models.BoxSession
import net.gotev.uploadservice.UploadTask
import net.gotev.uploadservice.data.UploadFile
import net.gotev.uploadservice.network.HttpStack
import net.gotev.uploadservice.network.ServerResponse

class BoxUploadTask : UploadTask(), BoxClientWrapper.Observer {

    private val boxParams by lazy {
        BoxUploadTaskParameters.createFromPersistableData(params.additionalParameters)
    }

    @Throws(Exception::class)
    override fun upload(httpStack: HttpStack) {
        val boxParams = boxParams
        BoxClientWrapper(
                uploadId = params.id,
                boxSession = BoxSession(context, boxParams.userID, boxParams.clientID, boxParams.clientSecret, boxParams.redirectUrl),
                shouldOverwrite = boxParams.shouldOverwrite,
                observer = this
        ).use { boxClient ->

            // this is needed to calculate the total bytes and the uploaded bytes, because if the
            // request fails, the upload method will be called again
            // (until max retries is reached) to retry the upload, so it's necessary to
            // know at which status we left, to be able to properly notify further progress.
            calculateUploadedAndTotalBytes()

            for (file in params.files) {
                if (!shouldContinue)
                    break

                if (file.successfullyUploaded)
                    continue

                boxClient.uploadFile(file, boxParams.folderId)
            }
        }
    }

    /**
     * Calculates the total bytes of this upload task.
     * This the sum of all the lengths of the successfully uploaded files and also the pending
     * ones.
     */
    private fun calculateUploadedAndTotalBytes() {
        resetUploadedBytes()

        var totalUploaded: Long = 0

        for (file in successfullyUploadedFiles) {
            totalUploaded += file.handler.size(context)
        }

        totalBytes = totalUploaded

        for (file in params.files) {
            totalBytes += file.handler.size(context)
        }

        onProgress(totalUploaded)
    }

    override fun onProgressChanged(client: BoxClientWrapper, numBytes: Long, bytesTotal: Long) {
        onProgress(numBytes - uploadedBytes)
        if (!shouldContinue) {
            client.close()
            exceptionHandling(Exception("User cancelled upload!"))
        }
    }

    override fun onSuccess(client: BoxClientWrapper, uploadFile: UploadFile, boxFileId: String) {
        params.files.filter { it.equals(uploadFile) }.first().successfullyUploaded = true
        onResponseReceived(
                ServerResponse(
                        200,
                        boxFileId.toByteArray(),
                        LinkedHashMap()
                )
        )
    }

    override fun onError(client: BoxClientWrapper, exception: Exception) {
        exceptionHandling(exception)
    }
}
