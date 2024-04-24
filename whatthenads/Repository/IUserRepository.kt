package com.yucox.whatthenads.Repository

import com.yucox.whatthenads.Model.UserInfo
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface IUserRepository {
    fun signOut()
    fun isAnyoneIn(): Int
    suspend fun logInCheck(mail: String?, pass: String?): Pair<Boolean, String?>
    suspend fun createAccount(mail: String?, pass: String?): Pair<Boolean, String?>
    suspend fun saveUserInfo(user: UserInfo): Pair<Boolean, String?>
    suspend fun fetchUserInfo(): UserInfo?
}
