package com.rhinepereira.saints.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

object RetrofitClient {

    private const val BASE_URL =
        "https://rhine-pereira.github.io/angelsandsaints_data/content/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun getApi(context: Context): ContentApi {
        val cacheSize = 50 * 1024 * 1024L // 50 MB
        val cache = Cache(File(context.cacheDir, "http-cache"), cacheSize)

        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor { chain ->
                var request = chain.request()
                if (!isNetworkAvailable(context)) {
                    // Offline: force cache
                    request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 365)
                        .build()
                }
                chain.proceed(request)
            }
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                
                val cacheControl = request.header("Cache-Control")
                if (cacheControl?.contains("no-cache") == true) {
                    response
                } else {
                    response.newBuilder()
                        .header("Cache-Control", "public, max-age=" + 60 * 60 * 24 * 365)
                        .removeHeader("Pragma")
                        .build()
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ContentApi::class.java)
    }
}
