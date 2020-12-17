package net.gotev.uploadservice.box

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import net.gotev.uploadservice.persistence.Persistable
import net.gotev.uploadservice.persistence.PersistableData

@Parcelize
data class BoxUploadTaskParameters(
    var userID: String = "",
    var clientID: String = "",
    var clientSecret: String = "",
    var redirectUrl: String = "",
    var shouldOverwrite: Boolean = false,
    var folderId: String = "0"

) : Parcelable, Persistable {
    companion object : Persistable.Creator<BoxUploadTaskParameters> {
        private object CodingKeys {
            const val userID = "userID"
            const val clientID = "clientID"
            const val clientSecret = "clientSecret"
            const val redirectUrl = "redirectUrl"
            const val shouldOverwrite = "shouldOverwrite"
            const val folderId = "folderId"
        }

        override fun createFromPersistableData(data: PersistableData) = BoxUploadTaskParameters(
                userID = data.getString(CodingKeys.userID),
                clientID = data.getString(CodingKeys.clientID),
                clientSecret = data.getString(CodingKeys.clientSecret),
                redirectUrl = data.getString(CodingKeys.redirectUrl),
                shouldOverwrite = data.getBoolean(CodingKeys.shouldOverwrite),
                folderId = data.getString(CodingKeys.folderId)
        )
    }

    override fun toPersistableData() = PersistableData().apply {
        putString(CodingKeys.userID, userID)
        putString(CodingKeys.clientID, clientID)
        putString(CodingKeys.clientSecret, clientSecret)
        putString(CodingKeys.redirectUrl, redirectUrl)
        putBoolean(CodingKeys.shouldOverwrite, shouldOverwrite)
        putString(CodingKeys.folderId, folderId)
    }
}
