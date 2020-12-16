package net.gotev.uploadservice.box

import android.content.Context
import android.util.Log
import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.listeners.ProgressListener
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.BoxRequestsFile
import net.gotev.uploadservice.data.UploadFile
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

import java.io.Closeable

class BoxClientWrapper(
        context: Context,
        boxSession: BoxSession,
        observer: Observer
) : Closeable
{

    private val context = context
    private val boxFileApi = BoxApiFile(boxSession)


    private lateinit var uploadTask: BoxFutureTask<BoxFile>

    interface Observer {
        fun onProgressChanged(client: BoxClientWrapper, numBytes: Long, bytesTotal: Long)
    }

    private val progressListener = object : ProgressListener {
        override fun onProgressChanged(numBytes: Long, bytesTotal: Long) {
            observer.onProgressChanged(this@BoxClientWrapper, numBytes, bytesTotal)
        }
    }

    /**
     * Method demonstrates file being uploaded using the box file api
     */
    @Throws(Exception::class)
    fun uploadFile(uploadFile: UploadFile) {
                try {
                    val file = File(uploadFile.path)
                    val uploadFileName = file.name
                    val uploadStream: InputStream = context.getResources().getAssets().open(uploadFileName)
                    val destinationFolderId = "0"
                    val uploadName = file.name
                    val request: BoxRequestsFile.UploadFile = boxFileApi.getUploadRequest(uploadStream, uploadName, destinationFolderId)
                    request.setProgressListener(progressListener)
                    uploadTask = request.toTask()
                    // val uploadFileInfo = request.send()
                } catch (e: BoxException) {
                    val error = e.asBoxError
                    if (error != null && error.status == HttpURLConnection.HTTP_CONFLICT) {
                        val conflicts = error.contextInfo.conflicts
                        if (conflicts != null && conflicts.size == 1 && conflicts[0] is BoxFile) {
                            uploadNewVersion(conflicts[0] as BoxFile)
                            return
                        }
                    }
                    Log.e("Wrapper/uploadFile", "Upload failed" + e.message)
                }
    }
    private fun uploadNewVersion(file: BoxFile) {
            val uploadFileName = "box_logo.png"
            val uploadStream: InputStream = context.getResources().getAssets().open(uploadFileName)
            val request = boxFileApi!!.getUploadNewVersionRequest(uploadStream, file.id)
            uploadTask = request.toTask()
    }

    override fun close() {
        uploadTask.cancel(true)
        // mFileApi.getAbortUploadSessionRequest(mFileApi.getUploadSession("a"))
    }
}