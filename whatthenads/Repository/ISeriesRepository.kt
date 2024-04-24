package com.yucox.whatthenads.Repository

import com.yucox.whatthenads.Model.SeriesInfo

interface ISeriesRepository {
    fun checkLoginStatus(): Boolean
    suspend fun saveSeries(seriesInfo: SeriesInfo): Boolean
    suspend fun checkExist(url: String): Boolean
    suspend fun fetchFavoriteSeries(): Pair<ArrayList<SeriesInfo>?, Boolean>
    suspend fun deleteFavorite(series: SeriesInfo): Pair<Boolean, String?>
    suspend fun checkAppVersion(): Boolean
}