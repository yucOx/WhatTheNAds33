package com.yucox.whatthenads.Model

import java.util.Date

data class SeriesInfo(
    val seriesName: String? = "",
    val episode: String? = "",
    val url : String? = "",
    val id : String? = "",
    val priority : String? = "",
    val date : Date? = Date()
) {
}