package net.gotev.uploadservice.box

import android.content.Context
import com.box.androidsdk.content.models.BoxSession
import net.gotev.uploadservice.UploadRequest
import net.gotev.uploadservice.UploadTask
import net.gotev.uploadservice.data.UploadFile
import java.io.File
import java.io.FileNotFoundException

class BoxUploadRequest(context: Context, boxSession: BoxSession) : UploadRequest<BoxUploadRequest>(context, "serverURL") {

    protected val boxparams = BoxUploadTaskParameters(
            userID = boxSession.userId,
            clientID = boxSession.clientId,
            clientSecret = boxSession.clientSecret,
            redirectUrl = boxSession.redirectUrl
    )

    override val taskClass: Class<out UploadTask>
        get() = BoxUploadTask::class.java

    override fun getAdditionalParameters() = boxparams.toPersistableData()

    /**
     * Sets whether a new version of the file should be overwritten if a file already exists
     */
    fun setShouldOverwrite(shouldOverwrite: Boolean): BoxUploadRequest {
        boxparams.shouldOverwrite = shouldOverwrite
        return this
    }

    /**
     * Sets the folder Id of the uploaded file. you should get this Id from your account
     */
    fun setFolderId(folderId: String): BoxUploadRequest {
        boxparams.folderId = folderId
        return this
    }

    /**
     * This would call the upload method of the upload task class (S3UploadTask)
     */
    override fun startUpload(): String {
        require(files.isNotEmpty()) { "Add at least one file to start box upload!" }
        files.forEach { uploadFile -> run {
            require(File(uploadFile.path).exists()) { "One or more files do not exist!" }
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
