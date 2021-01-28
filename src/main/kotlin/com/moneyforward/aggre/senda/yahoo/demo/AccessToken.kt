package com.moneyforward.aggre.senda.yahoo.demo

import com.google.gson.annotations.SerializedName

/**
 * Tokenエンドポイントからのレスポンスをマッピングしたデータオブジェクト
 */
data class AccessToken(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expireSecs: Int,
    @SerializedName("id_token")
    val idToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
) {

    fun update(refresh: TokenRefresh): AccessToken{
        return AccessToken(
            refresh.accessToken,
            refresh.tokenType,
            refresh.expireSecs,
            this.idToken,
            this.refreshToken
        )
    }

}

data class TokenRefresh(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expireSecs: Int
){}