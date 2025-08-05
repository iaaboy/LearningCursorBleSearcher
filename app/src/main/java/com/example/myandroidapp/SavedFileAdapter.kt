package com.example.myandroidapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SavedFileAdapter(
    private var files: List<SavedFile>,
    private val onFileClick: (SavedFile) -> Unit,
    private val onFileLongClick: (SavedFile) -> Boolean
) : RecyclerView.Adapter<SavedFileAdapter.SavedFileViewHolder>() {

    fun updateFiles(newFiles: List<SavedFile>) {
        files = newFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_file, parent, false)
        return SavedFileViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedFileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    inner class SavedFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileName: TextView = itemView.findViewById(R.id.fileName)
        private val fileType: TextView = itemView.findViewById(R.id.fileType)
        private val fileSize: TextView = itemView.findViewById(R.id.fileSize)
        private val fileDate: TextView = itemView.findViewById(R.id.fileDate)

        fun bind(file: SavedFile) {
            fileName.text = file.displayName
            fileType.text = file.type
            fileSize.text = file.formattedSize
            fileDate.text = file.formattedDate

            // 클릭 이벤트
            itemView.setOnClickListener {
                onFileClick(file)
            }

            // 길게 누르기 이벤트 (삭제 등)
            itemView.setOnLongClickListener {
                onFileLongClick(file)
            }
        }
    }
} 