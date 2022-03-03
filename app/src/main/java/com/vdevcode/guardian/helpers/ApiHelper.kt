package com.vdevcode.guardian.helpers


import com.squareup.okhttp.Credentials
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object ApiHelper {

    const val API_SCHEME = "https" // local use http
    const val API_SERVER = "meuguardian.com.br" // use https
    //https://orders?consumer_key=ck_19e0f9f58ec5598f7e1187233412a0112c897ea8&consumer_secret=cs_5ef4d97790c4c02b7fadd5a611e8721a6d5973cc

    //const val API_SERVER = "192.168.0.105"
    const val API_PATH = "wp-json/wc/v3"

    private val clientApi = OkHttpClient().apply {
        setConnectTimeout(10, TimeUnit.SECONDS)
        setWriteTimeout(25, TimeUnit.SECONDS)
        setReadTimeout(30, TimeUnit.SECONDS)
        retryOnConnectionFailure = true
    }
    //clientApi.build()

    fun GET(url: String,  onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        GlobalScope.launch {
            val request = Request.Builder()
                .get()
                .addHeader("Authorization", Credentials.basic("ck_19e0f9f58ec5598f7e1187233412a0112c897ea8", "cs_5ef4d97790c4c02b7fadd5a611e8721a6d5973cc"))
                .addHeader("accept", "application/json")
                //.url("https://meuguardian.com.br/wp-json/wc/v3/customers?email=aidanwbruno%40gmail.com&consumer_key=ck_19e0f9f58ec5598f7e1187233412a0112c897ea8&consumer_secret=cs_5ef4d97790c4c02b7fadd5a611e8721a6d5973cc&status=completed")
                .url(url)
                .build()

            clientCall(request, onSuccess, onError)
        }
    }

    private fun buildGetUrl(params: Map<String, String>): HttpUrl.Builder {
        val httpUrl = HttpUrl.Builder().apply {
            scheme(API_SCHEME)
            host(API_SERVER)
            addPathSegment(API_PATH)
        }
        params.entries.forEach {
            httpUrl.addQueryParameter(it.key, it.value)
        }
        return httpUrl
    }

    private fun clientCall(request: Request, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        try {
            //clientApi.connectionPool.evictAll()
            val res = clientApi.newCall(request).execute()
            res?.let { reponse ->
                if (reponse.isSuccessful) {
                    reponse.body()?.let {
                        val res = it.string()
                        reponse.body()?.close()
                        if (res.startsWith("Error")) {
                            onError.invoke(reponse.message())
                        } else {
                            onSuccess.invoke(res)
                        }
                    }
                } else {
                    reponse.body()?.close()
                    Helper.LogI("API RESULT ERROR: ${reponse.message()}")
                    onError.invoke(reponse.message())
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError.invoke("$ex.message")
        }
    }

    private fun normalizePaths(fullPath: String): List<String> {
        val paths = fullPath.split("/")
        if (paths.isNullOrEmpty() && !fullPath.isNullOrBlank()) {
            return listOf(fullPath)
        }
        return paths
    }
}