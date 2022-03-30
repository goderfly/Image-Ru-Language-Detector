import api.ApiRecognizeService
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit


object ImageRecognizeInteractor {
    const val YANDEX_RECOGNIZE_URL = "https://translate.yandex.net/"

    fun recognizeAll(imagePath: String, onProgress: (progress: Float) -> Unit) {
        val allImages = File(Configuration.savePath + imagePath).listFiles()
        allImages?.forEachIndexed { index, file ->
            runBlocking { requestForFile(file, imagePath, index, onProgress, allImages) }
        }

    }

    private suspend fun requestForFile(
        file: File,
        imagePath: String,
        index: Int,
        onProgress: (progress: Float) -> Unit,
        allImages: Array<File>
    ) {
        try {
            delay(3000L)
            val response = requestRecognize()?.recognizeImageLanguage(createFilePart(file))?.execute()
            if (response?.isSuccessful == true) {
                val string = response.body()?.string()

                if (string?.contains("\"detected_lang\":\"ru\"") == true) {
                    File(Configuration.savePath + "ru_$imagePath").mkdir()
                    Files.copy(file.toPath(), Paths.get(Configuration.savePath + "ru_$imagePath/${file.name}"))
                    println("response recognize ru")
                } else if (string?.contains("\"detected_lang\":\"en\"") == true) {
                    File(Configuration.savePath + "en_$imagePath").mkdir()
                    Files.copy(file.toPath(), Paths.get(Configuration.savePath + "en_$imagePath/${file.name}"))
                    println("response recognize en")
                } else {
                    File(Configuration.savePath + "other_$imagePath").mkdir()
                    Files.copy(file.toPath(), Paths.get(Configuration.savePath + "other_$imagePath/${file.name}"))
                    println("response recognize other $string")
                }
            } else {
                println("response recognize error ${response?.code()} ${response?.message()}")
                if (response?.code() == 429){
                    delay(15000L)
                    requestForFile(file, imagePath, index, onProgress, allImages)
                }
            }
        } catch (e: Exception) {
            println("error recognize image ${e.message}")
        } finally {
            if (index != 0) {
                onProgress.invoke((index + 1).toFloat() / allImages.size)
            }

        }
    }

    private fun createFilePart(data: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            "file",
            data.name,
            data.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
    }

    private fun requestRecognize(): ApiRecognizeService? {
        return Retrofit.Builder().run {
            baseUrl(YANDEX_RECOGNIZE_URL)
            client(
                OkHttpClient.Builder()
                    .connectTimeout(30L, TimeUnit.SECONDS)
                    .readTimeout(30L, TimeUnit.SECONDS)
                    .writeTimeout(30L, TimeUnit.SECONDS)
                    .build()
            )
            build()
        }.create(ApiRecognizeService::class.java)
    }
}