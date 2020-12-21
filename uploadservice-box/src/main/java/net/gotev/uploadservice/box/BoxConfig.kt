package net.gotev.uploadservice.box

import android.content.Context
import com.box.androidsdk.content.BoxApiFolder
import com.box.androidsdk.content.BoxConfig
import com.box.androidsdk.content.auth.BoxAuthentication
import com.box.androidsdk.content.models.BoxSession
import net.gotev.uploadservice.logger.UploadServiceLogger

class BoxConfig(context: Context, clientID: String, clientSecret: String, redirectURL: String) : BoxAuthentication.AuthListener {

    private val context = context
    private val clientID = clientID
    private val clientSecret = clientSecret
    private val redirectURL = redirectURL
    private lateinit var boxSession: BoxSession;

    companion object {
        private val TAG = BoxConfig::class.java.simpleName
    }

    /**
     * Authenticates user Box account and returns the box session.
     * The UI for authentication is handled automatically by Box
     */
    fun authenticate(): BoxSession {
        configureClient()
        return initSession()
    }

    /**
     * Creates a directory on the root
     * @param directoryName name of the directory
     */
    fun createDirectory(directoryName: String) {
        BoxApiFolder(boxSession).getCreateRequest("0", directoryName).send()
    }

    /**
     * Creates a directory under a given parent directory
     * @param directoryName name of the directory
     * @param parentDirectoryId Id of the parent directory
     */
    fun createDirectory(directoryName: String, parentDirectoryId: String) {
       BoxApiFolder(boxSession).getCreateRequest(parentDirectoryId, directoryName).send()
    }

    /**
     * Log out box session
     */
    fun logout() {
        boxSession.logout()
    }

    fun configureClient() {
        BoxConfig.IS_LOG_ENABLED = true
        BoxConfig.CLIENT_ID = clientID
        BoxConfig.CLIENT_SECRET = clientSecret
        BoxConfig.REDIRECT_URL = redirectURL
    }

    private fun initSession(): BoxSession {
        boxSession = BoxSession(context)
        boxSession!!.setSessionAuthListener(this)
        boxSession.authenticate(context)
        return boxSession
    }

    override fun onRefreshed(info: BoxAuthentication.BoxAuthenticationInfo?) {
        UploadServiceLogger.info(TAG, "N/A") { "Authentication refreshed" }
    }

    override fun onAuthCreated(info: BoxAuthentication.BoxAuthenticationInfo?) {
        UploadServiceLogger.info(TAG, "N/A") { "Authentication created" }
    }

    override fun onAuthFailure(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?) {
        UploadServiceLogger.info(TAG, "N/A") { "Authentication failed" }
    }

    override fun onLoggedOut(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?) {
        UploadServiceLogger.info(TAG, "N/A") { "Logged out" }
    }
}
