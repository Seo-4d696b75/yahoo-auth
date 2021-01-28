package com.moneyforward.aggre.senda.yahoo.demo

import com.google.gson.annotations.SerializedName
import java.net.URLEncoder


data class APIError(
    @SerializedName("error")
    val error: String,
    @SerializedName("error_description")
    val description: String,
    @SerializedName("error_code")
    val errorCode: Int
) {

    companion object {
        fun parseErrorIfAny(error: String?, description: String?, code: String?): APIError? {
            if ( arrayOf(error, description, code).any { it != null } ){
                val errorType = error ?: "unknown"
                val errorDescription = description?.let { URLEncoder.encode(it, Charsets.UTF_8) } ?: ""
                val errorCode = code?.toInt() ?: -1
                return APIError(errorType, errorDescription, errorCode)
            }
            return null
        }
    }

}