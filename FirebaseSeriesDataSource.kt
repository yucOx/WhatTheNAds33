package com.yucox.whatthenads.Repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.yucox.whatthenads.Model.SeriesInfo
import com.yucox.whatthenads.Util.Version
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseSeriesDataSource : ISeriesRepository {
    private val _database = FirebaseDatabase.getInstance()
    private val _ref = _database.getReference("Series")
    private val _auth = FirebaseAuth.getInstance()

    override fun checkLoginStatus(): Boolean {
        return _auth.currentUser != null
    }

    override suspend fun saveSeries(seriesInfo: SeriesInfo): Boolean {
        val key = _ref.push().key
        val newSeriesInfo = SeriesInfo(
            seriesInfo.seriesName,
            seriesInfo.episode,
            seriesInfo.url,
            key,
            _auth.currentUser?.email.toString()
        )
        return try {
            _ref.child(key.toString()).setValue(newSeriesInfo)
            true
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun checkExist(url: String): Boolean {
        return suspendCoroutine { Continuation ->
            _ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Continuation.resume(false)
                        return
                    }
                    for (snap in snapshot.children) {
                        val checkUrl = snap.child("url").getValue()
                        if (checkUrl == url) {
                            Continuation.resume(true)
                            return
                        }
                    }
                    Continuation.resume(false)
                }

                override fun onCancelled(error: DatabaseError) {
                    Continuation.resumeWithException(error.toException())
                }
            })
        }
    }

    override suspend fun fetchFavoriteSeries(): Pair<ArrayList<SeriesInfo>?, Boolean> {
        return suspendCoroutine { final ->
            val seriesList = ArrayList<SeriesInfo>()
            _ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        final.resume(null to false)
                        return
                    }
                    for (snap in snapshot.children) {
                        val priority = snap?.child("priority")?.getValue()
                        if (priority != null && !priority.equals(_auth.currentUser?.email.toString()))
                            continue

                        val series = snap.getValue<SeriesInfo>()
                        if (series != null)
                            seriesList.add(series)
                    }
                    final.resume(seriesList to true)
                }

                override fun onCancelled(error: DatabaseError) {
                    final.resume(null to false)
                }
            })
        }
    }

    override suspend fun deleteFavorite(series: SeriesInfo): Pair<Boolean, String?> {
        return suspendCoroutine { taskResult ->
            try {
                _ref.child(series.id.toString()).removeValue()
                taskResult.resume(true to null)
            } catch (e: Exception) {
                taskResult.resume(false to e.localizedMessage.toString())
            }
        }
    }

    override suspend fun checkAppVersion(): Boolean {
        val refVersion = _database.getReference("version")
        return suspendCoroutine { Continuation ->
            refVersion.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val versionNumber = snapshot.getValue<String>()
                    if (versionNumber.equals(Version.version)) {
                        Continuation.resume(true)
                    } else {
                        Continuation.resume(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Continuation.resume(false)
                }
            })
        }
    }
}