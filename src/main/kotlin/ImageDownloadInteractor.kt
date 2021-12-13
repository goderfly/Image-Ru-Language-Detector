import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


object ImageDownloadInteractor {

    fun downloadAll(imageUrls: List<String>, savePath: String, onProgress: (progress: Float) -> Unit) {
        File(savePath).mkdir()
        imageUrls.forEachIndexed { index, url ->
            try {
                URL(url).openStream().use { stream ->
                    Files.copy(stream, Paths.get("$savePath/${url.substringAfterLast("/")}"))
                }
            } catch (e: Exception) {
                println("error download image ${e.message}")
            } finally {
                if (index != 0) {
                    val allImagesSize = imageUrls.size.toFloat()
                    onProgress.invoke((index+1).toFloat() / allImagesSize)
                    println("progress ${ (index+1).toFloat() / allImagesSize }")
                }

            }
        }
    }

}