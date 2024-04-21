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

class UserRepository {
    private val _database = FirebaseDatabase.getInstance()
    private val _auth = FirebaseAuth.getInstance()
    private val _mainUserMail = _auth.currentUser?.email.toString()

    fun signOut() {
        _auth.signOut()
    }

    fun isAnyoneIn(): Int {
        return if (_auth.currentUser != null)
            1
        else 0
    }

    suspend fun logInCheck(mail: String?, pass: String?): Pair<Boolean, String?> {
        return try {
            _auth.signInWithEmailAndPassword(mail!!, pass!!).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    suspend fun createAccount(mail: String?, pass: String?): Pair<Boolean, String?> {
        return try {
            _auth.createUserWithEmailAndPassword(mail.toString(), pass.toString()).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    suspend fun saveUserInfo(user: UserInfo): Pair<Boolean, String?> {
        return try {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("UserInfo")
            ref.push().setValue(user).await()
            true to null

        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    suspend fun fetchUserInfo(): UserInfo? {
        val ref = _database.getReference("UserInfo")
        val mainMail = _auth.currentUser?.email.toString()
        return suspendCoroutine { Final ->
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Final.resume(null)
                        return
                    }
                    for (snap in snapshot.children) {
                        val tempMail = snap.child("mail").getValue()
                        if (tempMail?.equals(mainMail) == false)
                            continue
                        val userDetails = snap.getValue<UserInfo>()
                        Final.resume(userDetails)
                        return
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Final.resume(null)
                }
            })
        }
    }
}