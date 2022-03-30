import api.YandexRepository
import utils.StorableInt
import utils.YandexImageUtil

object FetchImageUrlsInteractor {
    var currentCaptchaAnswerCallback: (String) -> Unit = { }
    var currentCaptchaHappensCallback: (String) -> Unit = { }
    var callbackUrls: (imageUrls: List<String>) -> Unit = {}
    var callbackProgress: (progress: Float) -> Unit = {}
    var query: String = ""
    var loadedPages = 0
    var imageUrls = mutableListOf<String>()


    fun getImageUrls(
        startQuery: String,
        onProgress: (progress: Float) -> Unit,
        onLoadAllPages: (imageUrls: List<String>) -> Unit
    ) {
        callbackUrls = onLoadAllPages
        callbackProgress = onProgress
        query = startQuery
        loadedPages = 0
        imageUrls.clear()
        getImageUrls(0)
    }

    private fun getImageUrls(page: Int) {
        YandexRepository.getInstance().getImageData(
            query = query, page,
            onResult = { isSuccess, response ->
                if (isSuccess) {
                    response?.blocks?.let {
                        onFetchComplete(it[0])
                    }
                }

            }, eventListener = captchaListener
        )
    }

    private fun onFetchComplete(imageBlock: Blocks) {
        val html = imageBlock.html
        loadedPages++
        callbackProgress.invoke(loadedPages / Configuration.pageDeep.toFloat())

        YandexImageUtil.getImageItemListFromHtml(
            html = html,
            startIndexingItemsFromScratch = loadedPages == 0
        ).forEach {
            imageUrls.add(it.dups.first().url)
        }
        if ((loadedPages != imageBlock.params.lastPage) && (loadedPages < Configuration.pageDeep)) {
            println("load new page")
            getImageUrls(loadedPages)
        } else {
            println("end loading")
            callbackUrls.invoke(imageUrls)
        }
    }

    fun getCaptchaListener(function: (String) -> Unit) {
        currentCaptchaHappensCallback = function
    }


    val captchaListener = object : YandexRepository.CaptchaEventListener {
        override fun onCaptchaEvent(
            captchaImgUrl: String,
            isRepeatEvent: Boolean,
            onResult: (captchaValue: String) -> Unit
        ) {
            System.out.println("isSuccess $captchaImgUrl")
            currentCaptchaHappensCallback.invoke(captchaImgUrl)
            currentCaptchaAnswerCallback = onResult
        }

    }
}
