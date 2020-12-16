package net.gotev.uploadservice.box

import android.content.Context
import com.box.androidsdk.content.models.BoxSession
import net.gotev.uploadservice.UploadRequest
import net.gotev.uploadservice.UploadTask
import net.gotev.uploadservice.data.UploadFile
import java.io.File
import java.io.FileNotFoundException

class BoxUploadRequest(context: Context, boxSession: BoxSession): UploadRequest<BoxUploadRequest>(context, "") {

    protected val boxparams = BoxUploadTaskParameters(
            userID = boxSession.userId,
            clientID = boxSession.clientId,
            clientSecret = boxSession.clientSecret,
            redirectUrl = boxSession.redirectUrl
    );

    override val taskClass: Class<out UploadTask>
        get() = BoxUploadTask::class.java

    override fun getAdditionalParameters() = boxparams.toPersistableData()

    fun setCredentials(clientID: String, clientSecret: String): BoxUploadRequest {
        require(clientID.isNotBlank()) { "Specify clientID" }
        require(clientSecret.isNotBlank()) { "Specify clientSecret!" }
        boxparams.clientID = clientID
        boxparams.clientSecret = clientSecret
        return this
    }


    /**
     * This would call the upload method of the upload task class (S3UploadTask)
     */
    override fun startUpload(): String {
        require(files.isNotEmpty()) { "Add at least one file to start box upload!" }
        files.forEach { uploadFile -> run {
            require (File(uploadFile.path).exists()) { "One or more files do not exist!" }
        }
        }
        return super.startUpload()
    }

    @Throws(FileNotFoundException::class)
    fun addFileToUpload(filePath: String): BoxUploadRequest {
        files.add(UploadFile(filePath))
        return this
    }
}