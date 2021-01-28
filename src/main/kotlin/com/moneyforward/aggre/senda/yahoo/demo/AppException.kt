package com.moneyforward.aggre.senda.yahoo.demo

import java.lang.Exception

open class AppException(mes: String) : Exception(mes) {

}

class AuthAPIException(
    val error: APIError
    ) : AppException(error.toString()){}

class ResponseException(
    val status: Int,
    val mes: String?
) : AppException("status_code=${status}, message=${mes}")