package net.gotev.uploadservice.box

import com.box.androidsdk.content.auth.BoxAuthentication

/**
 * This is an interface used to propagate BoxAuthentication.AuthListener events.
 */
interface BoxAuthenticationObserver {
    fun onRefreshed(info: BoxAuthentication.BoxAuthenticationInfo?)
    fun onAuthCreated(info: BoxAuthentication.BoxAuthenticationInfo?)
    fun onAuthFailure(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?)
    fun onLoggedOut(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?)
}
