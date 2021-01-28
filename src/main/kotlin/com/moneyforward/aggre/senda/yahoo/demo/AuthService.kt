package com.moneyforward.aggre.senda.yahoo.demo

import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
@Scope("singleton")
class AuthService {

    @Value("\${app.yahoo.token_endpoint}")
    private lateinit var tokenURL: String

    @Value("\${app.yahoo.client_id}")
    private lateinit var clientID: String

    @Value("\${app.yahoo.secrete}")
    private lateinit var clientSecrete: String

    @Value("\${app.yahoo.redirect_uri}")
    private lateinit var redirectURL: String

    @Value("\${app.yahoo.user_info_endpoint}")
    private lateinit var userInfoURL: String

    private val basicAuth: String by lazy {
        Base64.getEncoder().encodeToString("${clientID}:${clientSecrete}".toByteArray(Charsets.UTF_8))
    }

    private val httpClient: OkHttpClient by lazy{
        OkHttpClient.Builder()
            .connectTimeout(1000, TimeUnit.MILLISECONDS)
            .build()
    }

    private fun requireSuccessfulBody(res: Response): String{
        return when(res.code){
            200 -> res.body?.string() ?: throw AppException("empty response body from authorization end-point")
            400 -> res.body?.let {
                val error = Gson().fromJson(it.string(), APIError::class.java)
                throw AuthAPIException(error)
            }?: throw ResponseException(400, null)
            else -> throw ResponseException(res.code, res.body?.string())
        }
    }

    fun getAccessToken(code: String): AccessToken{
        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("redirect_uri", redirectURL)
            .add("code", code)
            .build()
        val request = Request.Builder()
            .url(tokenURL)
            .header("Authorization","Basic $basicAuth")
            .post(body)
            .build()
        val res = httpClient.newCall(request).execute()
        return Gson().fromJson(requireSuccessfulBody(res), AccessToken::class.java)
    }

    fun refreshAccessToken(old: AccessToken): AccessToken{
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", old.refreshToken)
            .build()
        val request = Request.Builder()
            .url(tokenURL)
            .header("Authorization","Basic $basicAuth")
            .post(body)
            .build()
        val res = httpClient.newCall(request).execute()
        val refresh = Gson().fromJson(requireSuccessfulBody(res), TokenRefresh::class.java)
        return old.update(refresh)
    }

    fun getUserInfo(token: AccessToken): UserAttr{
        val request = Request.Builder()
            .url(userInfoURL)
            .header("Authorization", "Bearer ${token.accessToken}")
            .get()
            .build()
        val res = httpClient.newCall(request).execute()
        return Gson().fromJson(requireSuccessfulBody(res), UserAttr::class.java)
    }

}