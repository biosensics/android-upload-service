package net.gotev.uploadservice.box

import com.box.androidsdk.content.models.BoxSession
import net.gotev.uploadservice.UploadTask
import net.gotev.uploadservice.network.HttpStack

class BoxUploadTask: UploadTask(), BoxClientWrapper.Observer {

    private val boxParams by lazy {
        BoxUploadTaskParameters.createFromPersistableData(params.additionalParameters)
    }



    @Throws(Exception::class)
    override fun upload(httpStack: HttpStack) {
        val boxParams = boxParams;
        BoxClientWrapper(
                context = context,
                boxSession = BoxSession(context, boxParams.userID, boxParams.clientID, boxParams.clientSecret, boxParams.redirectUrl),
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

                boxClient.uploadFile(
                        file
                )
                file.successfullyUploaded = true
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
        onProgress(numBytes)
        if (!shouldContinue) {
            client.close()
            // exceptionHandling(Exception("User cancelled upload!"))
        }
    }
}