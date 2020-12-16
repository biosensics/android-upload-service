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

) : Parcelable, Persistable {
    companion object : Persistable.Creator<BoxUploadTaskParameters> {
        private object CodingKeys {
            const val userID = "userID"
            const val clientID = "clientID"
            const val clientSecret = "clientSecret"
            const val redirectUrl = "redirectUrl"
        }

        override fun createFromPersistableData(data: PersistableData) = BoxUploadTaskParameters(
                userID = data.getString(CodingKeys.userID),
                clientID = data.getString(CodingKeys.clientID),
                clientSecret = data.getString(CodingKeys.clientSecret),
                redirectUrl = data.getString(CodingKeys.redirectUrl)
        )
    }

    override fun toPersistableData() = PersistableData().apply {
        putString(CodingKeys.userID, userID)
        putString(CodingKeys.clientID, clientID)
        putString(CodingKeys.clientSecret, clientSecret)
        putString(CodingKeys.redirectUrl, redirectUrl)
    }
}