package com.yucox.whatthenads.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.whatthenads.Model.SeriesInfo
import com.yucox.whatthenads.View.MainActivity
import com.yucox.whatthenads.View.SelectActivity
import com.yucox.whatthenads.databinding.ListSeriesItemBinding

class ListFavoritesAdapter(
    private val context: Context,
    private val seriesList: ArrayList<SeriesInfo>,
    private val deleteSeries: (SeriesInfo) -> Unit
) :
    RecyclerView.Adapter<ListFavoritesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListSeriesItemBinding.inflate(inflater, parent, false)
        return (ViewHolder(binding))
    }

    override fun getItemCount(): Int {
        return seriesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val series = seriesList[position]
        holder.binding.seriesNameTv.text = series.seriesName
        holder.binding.episodeTv.text = series.episode

        holder.binding.selectFrameCl.setOnClickListener {
            if (series.url.isNullOrEmpty())
                return@setOnClickListener

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("url", series.url)
            context.startActivity(intent)
            (context as SelectActivity).finish()
        }

        holder.binding.deleteFavorite.setOnClickListener {
            if (series.url.isNullOrEmpty())
                return@setOnClickListener

            MaterialAlertDialogBuilder(context as SelectActivity)
                .setTitle("Listeden kaldırmak istediğinize emin misiniz?")
                .setNegativeButton("Evet") { _, _ ->
                    deleteSeries(series)
                }.setPositiveButton("Hayır") { _, _ ->
                }.show()
        }
    }


    class ViewHolder(val binding: ListSeriesItemBinding) : RecyclerView.ViewHolder(binding.root) {
    }

}