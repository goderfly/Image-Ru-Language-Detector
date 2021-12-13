package api.model

data class SerpItem(
    val preview: ArrayList<Preview>,
    val dups: List<Preview>,
    val thumb: Thumb,
    val snippet: Snippet
)

data class Preview(
    val url: String,
    val fileSizeInBytes: Int,
    val w: Int,
    val h: Int,
    val origin: Origin?
)

data class Origin(
    val w: Int,
    val h: Int,
    val url: String
)

data class Thumb(
    val url: String,
    val size: Size
)

data class Size(
    val width: Int,
    val height: Int
)

data class Snippet(
    val title: String,
//    val hasTitle: Boolean,
    val text: String,
    val url: String
//    val domain: String,
//    val redirUrl: String
)