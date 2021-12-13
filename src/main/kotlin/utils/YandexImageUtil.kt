package utils

import api.model.ImageData
import api.model.ImageItem
import api.model.SerpItem
import api.model.Thumb
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class YandexImageUtil {

    companion object {
        private var lastItemIndex = 0

        /**
         * Retrieves raw data and prepares it by filtering
         * duplicates and images that are too small
         *
         * @param html the html code from the Yandex images page
         *
         * @return list of ImageItem with prepared data for future use
         */
        fun getImageItemListFromHtml(
                html: String,
                startIndexingItemsFromScratch: Boolean = false
        ): List<ImageItem> {
            val imageList: ArrayList<ImageItem> = arrayListOf()
            var imageItem: ImageItem
            if (startIndexingItemsFromScratch) lastItemIndex = 0

            getSerpListFromHtml(html).forEachIndexed { itemNum, item ->
                val allImages: ArrayList<ImageData> = ArrayList()
                val allPreview = (item.preview + item.dups)
                    .distinctBy { it.origin?.url?: it.url }
                    .filter { it.origin?.w?: it.w > 300
                        && it.origin?.h?: it.h > 300 }
                allPreview.forEach { preview ->
                    if (preview.origin != null) {
                        allImages.add(
                            ImageData(
                                preview.origin.w, preview.origin.h,
                                preview.fileSizeInBytes,
                                preview.origin.url
                            )
                        )
                    } else {
                        allImages.add(
                            ImageData(
                                preview.w, preview.h,
                                preview.fileSizeInBytes,
                                preview.url
                            )
                        )
                    }
                }

                if (allImages.isNotEmpty()) {
                    val thumb = Thumb("https:" + item.thumb.url, item.thumb.size)
                    imageItem =
                        ImageItem(
                            itemNum + lastItemIndex,
                            item.snippet.title,
                            item.snippet.url,
                            allImages.apply {
                                sortByDescending { it.width }
                            },
                            thumb
                        )

                    imageList.add(imageItem)
                }
            }

            lastItemIndex += imageList.size

            return imageList
        }

        /**
         * Convert html class serp-list to list of SerpItem data class
         *
         * @param html the html code from the Yandex images page
         */
        private fun getSerpListFromHtml(html: String): List<SerpItem> {
            val itemList = arrayListOf<SerpItem>()
            val regItem = Regex("""data-bem='..serp-item.:(.+?),"detail_url".+?'""")
            val dataList = regItem.findAll(html)

            dataList.forEach { data ->
                val resultJSON = data.groupValues[1] + "}"
                val serpItem: SerpItem = Gson().fromJson(resultJSON, SerpItem::class.java)
                itemList.add(serpItem)
            }

            return itemList
        }
    }
}