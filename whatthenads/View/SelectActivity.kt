package com.yucox.whatthenads.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.pillpulse.View.LoginActivity
import com.yucox.whatthenads.Adapter.ListFavoritesAdapter
import com.yucox.whatthenads.Model.SeriesInfo
import com.yucox.whatthenads.R
import com.yucox.whatthenads.Repository.UserRepository
import com.yucox.whatthenads.Util.Websites.DIZIBOX
import com.yucox.whatthenads.ViewModel.SeriesViewModel
import com.yucox.whatthenads.databinding.ActivitySelectBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.logging.Handler

class SelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectBinding
    private lateinit var viewModel: SeriesViewModel
    private lateinit var adapter: ListFavoritesAdapter
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val enterAnim = AnimationUtils.loadAnimation(
            this,
            androidx.appcompat.R.anim.abc_slide_in_top
        )
        val exitAnimation = AnimationUtils.loadAnimation(
            this,
            androidx.appcompat.R.anim.abc_slide_out_top
        )


        viewModel = ViewModelProvider(this).get(SeriesViewModel::class.java)
        viewModel.checkAppVersion()
        viewModel.versionControl.observe(this) {
            if (!it) {
                binding.versionControlIv.startAnimation(enterAnim)
                binding.versionControlIv.setImageResource(R.drawable.new_version_avaible)
            } else {
                binding.versionControlIv.startAnimation(enterAnim)
                binding.versionControlIv.setImageResource(R.drawable.version_approved)

                mainScope.launch {
                    delay(1500)
                    binding.versionControlIv.startAnimation(exitAnimation)
                    binding.versionControlIv.visibility = View.GONE
                }


                val result = viewModel.checkLogin()
                if (result) {
                    viewModel.getMainUserInfo()
                    viewModel.getFavoriteSeries()
                    binding.diziboxMainPageBtn.visibility = View.VISIBLE
                    binding.comp1.visibility = View.VISIBLE
                    binding.comp2.visibility = View.VISIBLE
                } else {
                    binding.diziboxMainPageBtn.visibility = View.VISIBLE
                    binding.comp1.visibility = View.VISIBLE
                    binding.comp2.visibility = View.VISIBLE
                }

                viewModel.mainUser.observe(this) { main ->
                    if (!main.name.isNullOrEmpty()) {
                        binding.nameTv.text = " ${main.name} ${main.surname}"
                    }
                }

                val notAvaible = SeriesInfo("Şu anda kaydettiğiniz", "dizi bulunmamakta...")
                val notAvaible2 = SeriesInfo("Kaydettikleriniz burada", "gözükecek")
                val showAdvertise = ArrayList<SeriesInfo>().apply {
                    add(notAvaible)
                    add(notAvaible2)
                }
                viewModel.updateSeriesList(showAdvertise)

                viewModel.seriesList.observe(this) { seriesList ->
                    initRecyclerView(seriesList)
                }

                binding.logOutBtn.setOnClickListener {
                    UserRepository().signOut()
                    val intent = Intent(
                        this,
                        LoginActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                }

                binding.diziboxMainPageBtn.setOnClickListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("url", DIZIBOX)
                    startActivity(intent)
                    finish()
                }
            }
        }

    }

    private fun initRecyclerView(seriesList: ArrayList<SeriesInfo>) {
        adapter = ListFavoritesAdapter(
            this,
            seriesList
        ) { series ->
            viewModel.removeFavorite(series)
        }
        binding.favoritesRv.adapter = adapter
        binding.favoritesRv.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL, false
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.viewModelScope.cancel()
        mainScope.cancel()
    }
}