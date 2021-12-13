package api

import YandexResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiImageService {

    @GET("images/search")
    fun getJSONSearchResult(
        @Header("Cookie") cookie: String = "ipnd=0; " + "is_gdpr=0; " + "is_gdpr_b=COHeURCaVg==; " + "yp=1955547820.sp.aflt%3A1639323820#${System.currentTimeMillis()/1000}.mct.null#${System.currentTimeMillis()/1000}.mcl.#${System.currentTimeMillis()/1000}.mcv.0#${System.currentTimeMillis()/1000}.szm.1%3A1920x1080%3A1920x341; " + "gdpr=0; ",
        @Query("format") format: String = "json",
        @Query("request") add: String = "{\"blocks\":[{\"block\":\"serp-list_infinite_yes\"" +
                ",\"params\":{\"initialPageNum\":0},\"version\":2}]" +
                ",\"bmt\":{\"lb\":\"70xek^jjDN({yjI=52Fx\"}" +
                ",\"amt\":{\"las\":\"justifier-height=1;thumb-underlay=1;justifier-setheight=1;fitimages-height=1;justifier-fitincuts=1\"}}",
        //@Query("yu") yu: String = "1778190371562362282",
        @Query("p") page: Int = 0,
        @Query("text") search: String
    ): Call<YandexResponse>

    @GET("{path}")
    fun getHtml(
        @Path(value = "path", encoded = true) path: String,
        @Query("format") format: String = "json"
    ): Call<ResponseBody>

    @GET("{path}")
    fun getYandexResponse(
        @Path(value = "path", encoded = true) path: String,
        @Query("format") format: String = "json"
    ): Call<YandexResponse>

    @GET("https://{host}/checkcaptcha")
    fun sendYandexCaptchaForYandexResponse(
        @Path(value = "host", encoded = true) host: String,
        @Query("key") key: String,
        @Query("retpath", encoded = true) returnUrl: String,
        @Query("rep") value: String,
        @Query("format") format: String = "json"
    ): Call<YandexResponse>

    @GET("https://{host}/checkcaptcha")
    fun sendYandexCaptchaForHtml(
        @Path(value = "host", encoded = true) host: String,
        @Query("key") key: String,
        @Query("retpath", encoded = true) returnUrl: String,
        @Query("rep") value: String,
        @Query("format") format: String = "json"
    ): Call<ResponseBody>
}
