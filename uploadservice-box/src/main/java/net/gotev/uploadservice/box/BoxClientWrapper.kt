package net.gotev.uploadservice.box

import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.listeners.ProgressListener
import com.box.androidsdk.content.models.BoxSession
import com.box.androidsdk.content.models.BoxFile
import com.box.androidsdk.content.requests.BoxRequestsFile
import com.box.androidsdk.content.requests.BoxResponse
import net.gotev.uploadservice.data.UploadFile
import net.gotev.uploadservice.logger.UploadServiceLogger
import java.io.File
import java.net.HttpURLConnection

import java.io.Closeable
import java.util.concurrent.CancellationException

class BoxClientWrapper(
    uploadId: String,
    boxSession: BoxSession,
    shouldOverwrite: Boolean,
    observer: Observer
) : Closeable {

    private val boxFileApi = BoxApiFile(boxSession)
    private val shouldOverwrite = shouldOverwrite
    private lateinit var uploadingFile: UploadFile

    companion object {
        private val TAG = BoxClientWrapper::class.java.simpleName
    }

    private lateinit var uploadTask: BoxFutureTask<BoxFile>

    interface Observer {
        fun onProgressChanged(client: BoxClientWrapper, numBytes: Long, bytesTotal: Long)
        fun onCompleted(client: BoxClientWrapper, uploadFile: UploadFile)
        fun onError(client: BoxClientWrapper, exception: Exception)
    }

    private val progressListener = object : ProgressListener {
        override fun onProgressChanged(numBytes: Long, bytesTotal: Long) {
            observer.onProgressChanged(this@BoxClientWrapper, numBytes, bytesTotal)
        }
    }

    private val completionListener = object : BoxFutureTask.OnCompletedListener<BoxFile> {
        override fun onCompleted(response: BoxResponse<BoxFile>?) {
            if (response != null && response.isSuccess) {
                    observer.onCompleted(this@BoxClientWrapper, uploadingFile)
            } else if (response?.exception?.cause?.javaClass?.name.equals(CancellationException::class.qualifiedName)) {
                // User cancelled the upload
                // Do nothing
            } else {
                val error = (response?.exception as BoxException).asBoxError
                if (error != null && error.status == HttpURLConnection.HTTP_CONFLICT) {
                    val conflicts = error.contextInfo.conflicts
                    if (conflicts != null && conflicts.size == 1 && conflicts[0] is BoxFile) {
                        if (shouldOverwrite) {
                            uploadNewVersion((response.request as BoxRequestsFile.UploadFile).file, conflicts[0] as BoxFile)
                        } else {
                            observer.onCompleted(this@BoxClientWrapper, uploadingFile)
                        }
                    }
                } else {
                    UploadServiceLogger.error(TAG, uploadId, response.exception) { "Upload failed" }
                    observer.onError(this@BoxClientWrapper, exception = response.exception)
                }
            }
        }
    }

    /**
     * Method demonstrates file being uploaded using the box file api
     */
    @Throws(Exception::class)
    fun uploadFile(uploadFile: UploadFile, folderId: String) {
        uploadingFile = uploadFile
        val file = File(uploadFile.path)
        val request: BoxRequestsFile.UploadFile = boxFileApi.getUploadRequest(file, folderId)
        request.setProgressListener(progressListener)
        uploadTask = request.toTask()
        uploadTask.addOnCompletedListener(completionListener)
        uploadTask.run()
    }

    /**
     * This would override an existing file. Note the upload will be started once again from the beginning
     *
     */
    private fun uploadNewVersion(file: File, boxfile: BoxFile) {
            val request = boxFileApi.getUploadNewVersionRequest(file, boxfile.id)
            request.setProgressListener(progressListener)
            val returnedBoxfile = request.send()
            completionListener.onCompleted(BoxResponse(returnedBoxfile, null, request))
    }

    override fun close() {
        uploadTask.cancel(true)
    }
}
