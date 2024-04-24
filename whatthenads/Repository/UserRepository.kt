package com.yucox.whatthenads.Repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.yucox.whatthenads.Model.UserInfo

import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepository(private val dataSource: IUserRepository) : IUserRepository {
    override fun signOut() {
        return dataSource.signOut()
    }

    override fun isAnyoneIn(): Int {
        return dataSource.isAnyoneIn()
    }

    override suspend fun logInCheck(mail: String?, pass: String?): Pair<Boolean, String?> {
        return dataSource.logInCheck(mail, pass)
    }

    override suspend fun createAccount(mail: String?, pass: String?): Pair<Boolean, String?> {
        return dataSource.createAccount(mail, pass)
    }

    override suspend fun saveUserInfo(user: UserInfo): Pair<Boolean, String?> {
        return dataSource.saveUserInfo(user)
    }

    override suspend fun fetchUserInfo(): UserInfo? {
        return dataSource.fetchUserInfo()
    }
}
