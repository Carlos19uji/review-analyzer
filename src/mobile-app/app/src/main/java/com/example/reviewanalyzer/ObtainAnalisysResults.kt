package com.example.reviewanalyzer

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ApiService {
    @GET("results")
    fun getResults(): Call<ApiResponse>

    @POST("filter_by_word")
    fun getRelatedWords(
        @Query("word") word: String,
        @Body reviews: Map<Int, String>
    ): Call<RelatedWords>

    @POST("get_common_words")
    fun getCommonWords(
        @Body reviews: Map<Int, String>
    ): Call<RelatedWords>
}

data class ReviewData(
    val text: String,
    val sentiment: String,
    val emotion: String
)

data class ApiResponse(
    val analysis_results: Map<String, ReviewData>,
    val common_words: Map<String, Int>
)

data class RelatedWords(
    val related_words: Map<String, Int>
)

private var Reviews: Map<String, ReviewData>? = null


object RetrofitClient {
    private const val BASE_URL = "https://10.122.12.192:5000/" // ✅ HTTPS correcto

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(100, java.util.concurrent.TimeUnit.MINUTES)  // Timeout de conexión
                .writeTimeout(100, java.util.concurrent.TimeUnit.MINUTES)    // Timeout de escritura
                .readTimeout(100, java.util.concurrent.TimeUnit.MINUTES)     // Timeout de lectura
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient()) // ⬅️ Permite certificados no válidos
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

fun fetchAnalysisResults(context: Context, onResult: (ApiResponse?) -> Unit) {
    Reviews?.let {
        Log.d("API", "Usando datos en caché")
        return
    }

    RetrofitClient.apiService.getResults().enqueue(object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            if (response.isSuccessful) {
                val responseBody = response.body()


                if (responseBody != null) {
                    Reviews = responseBody.analysis_results  // Guardamos los resultados en caché
                    onResult(responseBody)

                    Notification.sendNotification(
                        context,
                        "Review analysis completed",
                        "The analysis of all reviews is complete!"
                    )
                } else {
                    Log.e("API", "La respuesta del servidor es nula")
                    onResult(null)
                }
            } else {
                Log.e("API", "Error en la respuesta: ${response.code()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Log.e("API", "Error al obtener datos", t)
            onResult(null)
        }
    })
}

fun getRelatedWords(context: Context, word: String, reviews: Map<Int, String>, onResult: (RelatedWords?) -> Unit) {
    RetrofitClient.apiService.getRelatedWords(word, reviews).enqueue(object : Callback<RelatedWords> {
        override fun onResponse(call: Call<RelatedWords>, response: Response<RelatedWords>) {
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("API", "Respuesta exitosa: $responseBody")

                if (responseBody != null) {
                    onResult(responseBody)
                } else {
                    Log.e("API", "La respuesta del servidor es nula")
                    onResult(null)
                }
            } else {
                Log.e("API", "Error en la respuesta: ${response.code()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<RelatedWords>, t: Throwable) {
            Log.e("API", "Error al obtener datos", t)
            onResult(null)
        }
    })
}

fun getCommonWords(reviews: Map<Int, String>, onResult: (RelatedWords?) -> Unit){
    RetrofitClient.apiService.getCommonWords(reviews).enqueue(object : Callback<RelatedWords> {
        override fun onResponse(call: Call<RelatedWords>, response: Response<RelatedWords>) {
            if (response.isSuccessful){
                val responseBody = response.body()
                Log.d("API", "Respuesta exitosa: $responseBody")

                if (responseBody != null) {
                    onResult(responseBody)
                } else {
                    Log.e("API", "La respuesta del servidor es nula")
                    onResult(null)
                }
            } else {
                Log.e("API", "Error en la respuesta: ${response.code()}")
                onResult(null)
            }
        }

        override fun onFailure(call: Call<RelatedWords>, t: Throwable) {
            Log.e("API", "Error al obtener datos", t)
            onResult(null)
        }
    })
}