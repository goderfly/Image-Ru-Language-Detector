import api.model.Captcha

data class YandexResponse(
        val blocks: List<Blocks>,
        val captcha: Captcha?
)

data class Blocks(
        val name: Name,
        val params: Params,
        val html: String
)

data class Name(val block: String)

data class Params(
        val count: Int,
        val lastPage: Int,
        val bundles: ArrayList<Any?>
)
