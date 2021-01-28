package com.moneyforward.aggre.senda.yahoo.demo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@RestController
class DemoController {

    @Autowired
    private lateinit var auth: AuthService

    @Value("\${app.yahoo.client_id}")
    private lateinit var clientID: String

    @Value("\${app.yahoo.authorization_endpoint}")
    private lateinit var authorizationURL: String

    @Value("\${app.yahoo.redirect_uri}")
    private lateinit var redirectURL: String

    // テストのため固定値を使用
    private val sessionID = "test-session"

    @Autowired
    private lateinit var session: HttpSession

    @GetMapping("/")
    fun getHello() : String {
        return "<html><body><h1>Hello OAuth 2.0!!</h1><a href=\"redirect\">Yahoo! ID 連携</a></body></html>"
    }

    /**
     * 認可サーバへリダイレクト
     */
    @GetMapping("/redirect")
    fun redirectAuthorize(response: HttpServletResponse){
        val url = "${authorizationURL}?response_type=code&client_id=${clientID}&redirect_uri=${redirectURL}&scope=openid%20profile%20email&state=${sessionID}"
        response.setHeader("Location", url)
        response.status = 302
    }

    /**
     * Authコールバック先
     */
    @GetMapping("/auth")
    fun getAuthorized(
        @RequestParam(required = true) state: String,
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) error: String?,
        @RequestParam(required = false, name = "error_description") errorMes: String?,
        @RequestParam(required = false, name = "error_code") errorCode: String?,
        attr: RedirectAttributes
    ): Any {
        // check session state
        if ( state != sessionID ){
            throw AppException("unknown state value: ${state}. Who are you?")
        }
        return APIError.parseErrorIfAny(error, errorMes, errorCode)?.let {
            throw AuthAPIException(it)
        } ?: code?.let {
            // TODO async call
            val token = auth.getAccessToken(it)
            session.setAttribute("token", token)
            "<html><body>" +
                    "<h1>Authorized!!</h1>" +
                    "<p>authorization_code: ${it}<br/>access_token:${token.accessToken}</p>" +
                    "<button onclick=\"location.href='/refresh'\">Refresh</button>" +
                    "<button onclick=\"location.href='/info'\">Get User's Info</button>" +
                    "</body></html>"
        } ?: throw AppException("code missing in response from authorization end-point")
    }

    @GetMapping("/refresh")
    fun getRefreshedToken(): String{
        val token = session.getAttribute("token") as AccessToken
        // TODO async call
        val newToken = auth.refreshAccessToken(token)
        session.setAttribute("token", newToken)
        return "<html><body>" +
                "<h1>Refreshed!!</h1>" +
                "<p>access_token:${newToken.accessToken}</p>" +
                "<button onclick=\"location.href='/refresh'\">Refresh</button>" +
                "<button onclick=\"location.href='/info'\">Get User's Info</button>" +
                "</body></html>"
    }

    @GetMapping("/info")
    fun getUserInfo(): String{
        val token = session.getAttribute("token") as AccessToken
        val info = auth.getUserInfo(token)
        return "<html><body>" +
                "<h1>Hello, ${info.getDisplayName()}</h1>" +
                "<div><img src=\"${info.profileImgURL}\" alt=\"profile image\" style=\"width: 50px; height: 50px;\"/></div>" +
                "<table><tbody>" +
                "<tr><td>uid:</td><td>${info.uid}</td></tr>" +
                "<tr><td>name:</td><td>${info.name}</td></tr>" +
                "<tr><td>nickname:</td><td>${info.nickname}</td></tr>" +
                "<tr><td>email:</td><td>${info.email}</td></tr>" +
                "</tbody></table>" +
                "<br/><a href=\"/\">Go back to to page</a></p>" +
                "</body></html>"
    }

    @ExceptionHandler(value = [AuthAPIException::class])
    fun getAuthFailure(e: AuthAPIException): String{
        val data = e.error
        return "<html><body>" +
                "<h1>Authorization error happened!!</h1>" +
                "<table><tbody>" +
                "<tr><td>error code:</td><td>${data.error}</td></tr>" +
                "<tr><td>description:</td><td>${data.description}</td></tr>" +
                "</tbody></table>" +
                "<p><a target=\"_blank\" href=\"https://openid.net/specs/openid-connect-core-1_0.html#AuthError\">more details</a>" +
                "<br/><a href=\"/\">Go back to to page</a></p>" +
                "</body></html>"
    }

    @ExceptionHandler(value = [Exception::class])
    fun getAppFailure(e: java.lang.Exception): String{

        return "<html><body>" +
                "<h1>Some error happened!!</h1>" +
                "<p>message: ${e.message}</p>" +
                "<p><a href=\"/\">Go back to to page</a></p>" +
                "</body></html>"
    }
}