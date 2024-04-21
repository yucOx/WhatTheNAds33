package com.yucox.whatthenads.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucox.whatthenads.Model.UserInfo
import com.yucox.whatthenads.Repository.UserRepository
import com.yucox.whatthenads.Util.Messages.TRYSAVEAGAIN
import com.yucox.whatthenads.Util.Messages.UNEXPECTEDERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    private val _user = MutableLiveData<UserInfo>()
    private val _pass = MutableLiveData<String>()
    private val _status = MutableLiveData<Int>()
    private val _error = MutableLiveData<String>()

    val status: LiveData<Int> = _status
    val error: LiveData<String> = _error

    private val _repository = UserRepository()

    fun isAnyoneIn(): Int {
        return _repository.isAnyoneIn()
    }

    private fun updateErrorMessage(errorMessage: String) {
        _error.value = errorMessage
    }

    fun updateUser(newUser: UserInfo, userPass: String) {
        _user.value = newUser
        _pass.value = userPass
    }

    fun createNewAccount() {
        viewModelScope.launch {
            val (result, exception) = withContext(Dispatchers.IO) {
                _repository.createAccount(_user.value?.mail, _pass.value)
            }
            if (result) {
                updateStatus(1)
            } else {
                updateErrorMessage(exception ?: UNEXPECTEDERROR)
            }
        }
    }

    private fun updateStatus(newStat: Int) {
        _status.value = newStat
    }

    fun saveUserInfo() {
        viewModelScope.launch {
            val (result, exception) = withContext(Dispatchers.IO) {
                _repository.saveUserInfo(_user.value!!)
            }
            if (!result) {
                updateErrorMessage((exception) ?: (UNEXPECTEDERROR))
                updateErrorMessage(TRYSAVEAGAIN)
                saveUserInfo()
            }
        }
    }

    fun logIn() {
        viewModelScope.launch {
            val (result, errorMessage) = withContext(Dispatchers.IO) {
                _repository.logInCheck(_user.value?.mail, _pass.value)
            }
            if (result) {
                updateStatus(1)
            } else {
                updateErrorMessage(errorMessage ?: UNEXPECTEDERROR)
            }
        }
    }
}