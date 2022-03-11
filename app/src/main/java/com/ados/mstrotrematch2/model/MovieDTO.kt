package com.ados.mstrotrematch2.model

/*data class MovieDTO (
    var videoId: String? = null,
    var imgurl: String? = null,
    var title: String? = null
) {
}*/

data class YoutubeApi (
    var q: String? = null,
    var part: String? = "snippet",
    var key: String? = "AIzaSyBpnA7k1IhucLSjsNTvwP0PSLS7vk8fBvQ", // 관리자모드
    var type: String? = "video",
    var maxResults: String? = "50",
    var regionCode: String? = "KR",
    var videoDuration: String? = null,
    var order: String? = null,
    var channelId: String? = null,
    var keyword: String? = null
) {
    fun getUrl(): String {
        var url = "https://www.googleapis.com/youtube/v3/search?"

        if (!q.isNullOrEmpty()) url += "q=$q&"
        else if (!keyword.isNullOrEmpty()) url += "q=$keyword&"

        if (!part.isNullOrEmpty()) url += "part=$part&"
        if (!key.isNullOrEmpty()) url += "key=$key&"
        if (!type.isNullOrEmpty()) url += "type=$type&"
        if (!maxResults.isNullOrEmpty()) url += "maxResults=$maxResults&"
        if (!regionCode.isNullOrEmpty()) url += "regionCode=$regionCode&"
        if (!videoDuration.isNullOrEmpty()) url += "videoDuration=$videoDuration&"
        if (!order.isNullOrEmpty()) url += "order=$order&"
        if (!channelId.isNullOrEmpty()) url += "channelId=$channelId&"

        return url
    }
}

data class MovieDTO (
    val kind: String? = null,
    val etag: String? = null,
    val nextPageToken: String? = null,
    val regionCode: String? = null,
    val pageInfo: PageInfo,
    var items: List<ItemList>
)

data class PageInfo (
    val totalResults: Int? = 0,
    val resultsPerPage: Int? = 0
)

data class ItemList (
    val kind: String? = null,
    val etag: String? = null,
    val id: ID,
    val snippet: Snippet
)

data class ID (
    val kind: String? = null,
    val videoId: String? = null
)

data class Snippet (
    val publishedAt: String? = null,
    val channelId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnails: Thumbnails,
    val channelTitle: String? = null,
    val liveBroadcastContent: String? = null
)

data class Thumbnails (
    val default:ThumbnailsDetail,
    val medium:ThumbnailsDetail,
    val high:ThumbnailsDetail
)

data class ThumbnailsDetail (
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)