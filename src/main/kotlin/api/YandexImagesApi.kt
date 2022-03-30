package api


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object YandexImagesApi {
    const val BASE_YANDEX_URL = "https://yandex.ru/"

    val instance: ApiImageService = Retrofit.Builder().run {
        baseUrl(BASE_YANDEX_URL)
        addConverterFactory(GsonConverterFactory.create(getGson()))
        client(getOkHttpClient(getEventListener()))
        build()
    }.create(ApiImageService::class.java)


    private fun getGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    private fun getEventListener(): EventListener {
        return object: EventListener() {
            override fun callFailed(call: Call, ioe: IOException) {
                System.out.println("connectionFailed: ${ioe.message}\ncheck internet connection")
            }
        }
    }

    private fun getOkHttpClient(eventListener: EventListener): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(10L, TimeUnit.SECONDS)
            .writeTimeout(10L, TimeUnit.SECONDS)
            .eventListener(eventListener)

        return clientBuilder.build()
    }
}