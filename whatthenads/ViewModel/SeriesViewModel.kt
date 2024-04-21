package com.yucox.whatthenads.ViewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucox.whatthenads.Model.SeriesInfo
import com.yucox.whatthenads.Model.UserInfo
import com.yucox.whatthenads.R
import com.yucox.whatthenads.Repository.SeriesRepository
import com.yucox.whatthenads.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SeriesViewModel : ViewModel() {
    private val _seriesInfo = MutableLiveData<SeriesInfo>()
    private val _seriesList = MutableLiveData<ArrayList<SeriesInfo>>()
    private val _repository = SeriesRepository()
    private val _message = MutableLiveData<String>()
    private val _mainUser = MutableLiveData<UserInfo>()
    private val _adList = MutableLiveData<ArrayList<String>>()
    private val _versionControl = MutableLiveData<Boolean>()

    val mainUser: LiveData<UserInfo> get() = _mainUser
    val message: LiveData<String> get() = _message
    val seriesList: LiveData<ArrayList<SeriesInfo>> get() = _seriesList
    val adList: LiveData<ArrayList<String>> get() = _adList
    val versionControl: LiveData<Boolean> get() = _versionControl

    fun updateSeries(newSeries: SeriesInfo) {
        _seriesInfo.value = newSeries
    }

    fun updateSeriesList(newSeriesList: ArrayList<SeriesInfo>) {
        _seriesList.value = newSeriesList
    }

    fun executeFilterList(context: Context) {
        val adListTemp = ArrayList<String>()
        val fileName = context.resources.openRawResource(R.raw.filterlist)
        fileName.bufferedReader().use {
            it.forEachLine { line ->
                adListTemp.add(line)
            }
        }
        _adList.value = adListTemp
    }

    private fun addAsBookmark() {
        _seriesInfo.value?.let {
            viewModelScope.launch {
                try {
                    _repository.saveSeries(it)
                    _message.value = "Kayıt başarılı."
                    _message.value = ""
                } catch (e: Exception) {
                    _message.value = e.localizedMessage?.toString()
                    _message.value = ""
                }

            }
        }
    }

    fun checkSaveStatus() {
        if (_seriesInfo.value?.id != null) {
            viewModelScope.launch {
                val userIn = _repository.checkLoginStatus()
                if (!userIn) {
                    _message.value = "Dizi kaydetmek için kayıt olmalısın"
                    _message.value = ""
                    return@launch
                }

                if (_seriesInfo.value?.seriesName.isNullOrEmpty()
                    || _seriesInfo.value?.seriesName!!.contains("null")
                ) {
                    _message.value = "Birkaç saniye sonra tekrar deneyin"
                    _message.value = ""
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    _repository.checkExist(_seriesInfo.value!!.url.toString())
                }
                if (!result) {
                    addAsBookmark()
                } else {
                    _message.value = "Zaten kayıtlı."
                    _message.value = ""
                }
            }
        }
    }

    fun getFavoriteSeries() {
        viewModelScope.launch {
            val (tempSeriesList, result) = withContext(Dispatchers.IO) {
                _repository.fetchFavoriteSeries()
            }
            if (result) {
                val sortedSeries = tempSeriesList?.sortedByDescending { it.date }

                _seriesList.value = ArrayList(sortedSeries)
            }
        }
    }

    fun checkLogin(): Boolean {
        return _repository.checkLoginStatus()
    }

    fun getMainUserInfo() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                UserRepository().fetchUserInfo()
            }

            if (result != null) {
                _mainUser.value = result
            }
        }
    }

    fun removeFavorite(series: SeriesInfo) {
        viewModelScope.launch {
            val (result, exception) = withContext(Dispatchers.IO) {
                _repository.deleteFavorite(series)
            }
            if (!result) {
                _message.value = exception
                _message.value = ""
            } else {
                val temp = ArrayList<SeriesInfo>(_seriesList.value)
                temp.remove(series)
                _seriesList.value = temp
            }
        }
    }

    fun checkAppVersion() {
        viewModelScope.launch {
            val result = _repository.checkAppVersion()
            _versionControl.value = result
        }
    }
}