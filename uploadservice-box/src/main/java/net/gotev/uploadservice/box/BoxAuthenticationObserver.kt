package net.gotev.uploadservice.box

import com.box.androidsdk.content.auth.BoxAuthentication

interface BoxAuthenticationObserver {
    fun onRefreshed(info: BoxAuthentication.BoxAuthenticationInfo?)
    fun onAuthCreated(info: BoxAuthentication.BoxAuthenticationInfo?)
    fun onAuthFailure(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?)
    fun onLoggedOut(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?)
}
