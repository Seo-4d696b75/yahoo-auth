package com.moneyforward.aggre.senda.yahoo.demo

import com.google.gson.annotations.SerializedName

/**
 * scope = [openid, profile, email] を想定したユーザ情報
 */
data class UserAttr(
    /**
     * scope: openid に対応したユーザの識別子
     */
    @SerializedName("sub")
    val uid: String,

    @SerializedName("name")
    val name: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("birthdate")
    val birthYear: Int?,
    @SerializedName("picture")
    val profileImgURL: String,

    @SerializedName("email")
    val email: String,
    @SerializedName("email_verified")
    val isEmailVerified: Boolean
) {

    fun getDisplayName(): String{
        return arrayOf(name, nickname, uid).find { it != null } ?: "none"
    }

}