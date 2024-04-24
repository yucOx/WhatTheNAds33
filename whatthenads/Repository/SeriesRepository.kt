package com.yucox.whatthenads.Repository

import com.yucox.whatthenads.Model.SeriesInfo

class SeriesRepository(private val dataSource: ISeriesRepository) : ISeriesRepository {
    override suspend fun saveSeries(seriesInfo: SeriesInfo): Boolean {
        return dataSource.saveSeries(seriesInfo)
    }

    override suspend fun checkExist(url: String): Boolean {
        return dataSource.checkExist(url)
    }

    override suspend fun fetchFavoriteSeries(): Pair<ArrayList<SeriesInfo>?, Boolean> {
        return dataSource.fetchFavoriteSeries()
    }

    override suspend fun deleteFavorite(series: SeriesInfo): Pair<Boolean, String?> {
        return dataSource.deleteFavorite(series)
    }

    override suspend fun checkAppVersion(): Boolean {
        return dataSource.checkAppVersion()
    }

    override fun checkLoginStatus(): Boolean {
        return dataSource.checkLoginStatus()
    }
}
