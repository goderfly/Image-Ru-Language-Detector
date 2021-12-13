package api.model



/**
 * Main class used to store search result element
 */
data class ImageItem(
    val itemNum: Int,
    val title: String,
    var sourceSite: String,
    val dups: MutableList<ImageData>,
    val thumb: Thumb
)

data class ImageData(
    val width: Int,
    val height: Int,
    val fileSizeInBytes: Int,
    val url: String
)