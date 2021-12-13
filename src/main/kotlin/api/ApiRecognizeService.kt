package api

import YandexResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiRecognizeService {
    @Multipart
    @POST("ocr/v1.1/recognize?srv=tr-image&lang=*")
    fun recognizeImageLanguage(@Part file: MultipartBody.Part): Call<ResponseBody>
}
