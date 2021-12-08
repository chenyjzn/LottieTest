package com.yuchen.bindingclick

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yuchen.bindingclick.databinding.AnimationTestHolderBinding
import com.yuchen.bindingclick.databinding.LottieTestHolderBinding

class LottieTestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LottieTestHolder(
            LottieTestHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
//        return AnimationTestHolder(
//            AnimationTestHolderBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
    }

    override fun getItemCount(): Int {
        return 50
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AnimationTestHolder -> {
                holder.bind()
            }
            is LottieTestHolder -> {
                holder.bind()
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is AnimationTestHolder -> {
                holder.recycle()
            }
        }
    }
}