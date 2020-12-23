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
        fun onSuccess(client: BoxClientWrapper, uploadFile: UploadFile, boxFileId: String)
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
                observer.onSuccess(this@BoxClientWrapper, uploadingFile, response.result.id)
            } else if (response?.exception?.cause?.javaClass?.name.equals(CancellationException::class.qualifiedName)) {
                // User cancelled the upload
                // Do nothing
            } else {
                val error = (response?.exception as BoxException).asBoxError
                if (error != null && error.status == HttpURLConnection.HTTP_CONFLICT) { // File already exists in the box
                    val conflicts = error.contextInfo.conflicts
                    if (conflicts != null && conflicts.size == 1 && conflicts[0] is BoxFile) {
                        if (shouldOverwrite) { // Upload new version
                            uploadNewVersion((response.request as BoxRequestsFile.UploadFile).file, conflicts[0] as BoxFile)
                        } else { // Do nothing and notify observers that upload is completed
                            observer.onSuccess(this@BoxClientWrapper, uploadingFile, response.result.id)
                        }
                    }
                } else { // Upload failed (network error, etc.)
                    UploadServiceLogger.error(TAG, uploadId, response.exception) { "Upload failed" }
                    observer.onError(this@BoxClientWrapper, exception = response.exception)
                }
            }
        }
    }

    /**
     * Uploads a file using the box file api
     * @param uploadFile the file to be uploaded
     * @param folderId the id of the folder that the uploading file would go to. If it is not set on upload request, it would upload to root folder
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
     * This would override an existing file.
     * The version of the file would increase after successful upload.
     * Note: the upload will be started once again from the beginning on this process.
     *
     */
    private fun uploadNewVersion(file: File, boxfile: BoxFile) {
        val request = boxFileApi.getUploadNewVersionRequest(file, boxfile.id)
        request.setProgressListener(progressListener)
        val returnedBoxfile = request.send()
        completionListener.onCompleted(BoxResponse(returnedBoxfile, null, request))
    }

    /**
     * cancel an upload in progress task
     */
    override fun close() {
        uploadTask.cancel(true)
    }
}
