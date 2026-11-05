package com.example.zerobook.data.remote

import com.example.zerobook.model.Book
import kotlinx.serialization.Serializable

/*
 * DTO = Data Transfer Object: khớp đúng cấu trúc JSON của API.
 * Mọi trường đều có giá trị mặc định vì API hay thiếu trường (sách không có ảnh, không có tác giả...).
 */

@Serializable
data class VolumesResponseDto(
    val totalItems: Int = 0,
    val items: List<VolumeDto> = emptyList(),
)

@Serializable
data class VolumeDto(
    val id: String,
    val volumeInfo: VolumeInfoDto = VolumeInfoDto(),
)

@Serializable
data class VolumeInfoDto(
    val title: String = "",
    val authors: List<String> = emptyList(),
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val imageLinks: ImageLinksDto? = null,
)

@Serializable
data class ImageLinksDto(
    val smallThumbnail: String? = null,
    val thumbnail: String? = null,
)

/** Chuyển DTO sang model của app, để phần còn lại của app không phụ thuộc vào định dạng API. */
fun VolumeDto.toDomain(): Book = Book(
    id = id,
    title = volumeInfo.title,
    authors = volumeInfo.authors,
    description = volumeInfo.description?.stripHtml()?.takeIf { it.isNotBlank() },
    // Google Books trả link http:// — Android chặn http mặc định nên đổi sang https://
    thumbnailUrl = (volumeInfo.imageLinks?.thumbnail ?: volumeInfo.imageLinks?.smallThumbnail)
        ?.replace("http://", "https://"),
    pageCount = volumeInfo.pageCount?.takeIf { it > 0 },
    publishedDate = volumeInfo.publishedDate,
    publisher = volumeInfo.publisher,
)

/** Mô tả sách ở API chi tiết có chứa thẻ HTML (<p>, <br>, <b>...) → bỏ đi để hiển thị chữ thường. */
private fun String.stripHtml(): String = this
    .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
    .replace(Regex("<[^>]+>"), "")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&amp;", "&")
    .trim()
