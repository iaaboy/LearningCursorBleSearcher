package com.example.myandroidapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SavedFileTableAdapter(
    private var files: List<SavedFile>,
    private val onFileClick: (SavedFile) -> Unit,
    private val onFileLongClick: (SavedFile) -> Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_FILE = 1
    }

    fun updateFiles(newFiles: List<SavedFile>) {
        files = newFiles
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_FILE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_saved_file_table_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_saved_file_table_row, parent, false)
                FileViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                // 헤더는 고정 텍스트
            }
            is FileViewHolder -> {
                holder.bind(files[position - 1]) // 헤더 때문에 -1
            }
        }
    }

    override fun getItemCount(): Int = files.size + 1 // 헤더 포함

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 헤더는 레이아웃에서 고정 텍스트로 처리
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileName: TextView = itemView.findViewById(R.id.tableFileName)
        private val fileType: TextView = itemView.findViewById(R.id.tableFileType)
        private val fileSize: TextView = itemView.findViewById(R.id.tableFileSize)
        private val fileDate: TextView = itemView.findViewById(R.id.tableFileDate)

        fun bind(file: SavedFile) {
            fileName.text = file.displayName
            fileType.text = file.type.uppercase()
            fileSize.text = file.formattedSize
            fileDate.text = file.formattedDate

            // 파일 형식에 따른 색상 설정
            when (file.type.lowercase()) {
                "csv" -> fileType.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                "json" -> fileType.setTextColor(itemView.context.getColor(android.R.color.holo_blue_dark))
                "txt" -> fileType.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                else -> fileType.setTextColor(itemView.context.getColor(R.color.primary_color))
            }

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