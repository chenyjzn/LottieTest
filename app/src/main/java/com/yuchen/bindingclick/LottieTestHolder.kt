package com.yuchen.bindingclick

import android.animation.Animator
import android.util.Log
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.yuchen.bindingclick.databinding.AnimationTestHolderBinding
import com.yuchen.bindingclick.databinding.LottieTestHolderBinding
import com.yuchen.view.Config
import com.yuchen.view.M17AnimationTextureView

class LottieTestHolder(private val binding: LottieTestHolderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind() {
        binding.animationView.addAnimatorListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator?) {
                Log.d("chenyjzn","onAnimationStart")
            }

            override fun onAnimationEnd(animation: Animator?) {
                Log.d("chenyjzn","onAnimationEnd")
            }

            override fun onAnimationCancel(animation: Animator?) {
                Log.d("chenyjzn","onAnimationCancel")
            }

            override fun onAnimationRepeat(animation: Animator?) {
                Log.d("chenyjzn","onAnimationRepeat")
            }

        })
        binding.animationView.setAnimation(R.raw.lf20_sehjh1kb)
        binding.animationView.loop(true)
        binding.animationView.playAnimation()
    }
}

class AnimationTestHolder(private val binding: AnimationTestHolderBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(){
        val resList = listOf(
            R.drawable.a00000,
            R.drawable.a00001,
            R.drawable.a00002,
            R.drawable.a00003,
            R.drawable.a00004,
            R.drawable.a00005,
            R.drawable.a00006,
            R.drawable.a00007,
            R.drawable.a00008,
            R.drawable.a00009,
            R.drawable.a00010,
            R.drawable.a00011,
            R.drawable.a00012,
            R.drawable.a00013,
            R.drawable.a00014,
            R.drawable.a00015,
            R.drawable.a00016,
            R.drawable.a00017,
            R.drawable.a00018,
            R.drawable.a00019,
            R.drawable.a00020,
            R.drawable.a00021,
            R.drawable.a00022,
            R.drawable.a00023,
            R.drawable.a00024,
            R.drawable.a00025,
            R.drawable.a00026,
            R.drawable.a00027,
            R.drawable.a00028,
            R.drawable.a00029,
            R.drawable.a00030,
            R.drawable.a00031,
            R.drawable.a00032,
            R.drawable.a00033,
            R.drawable.a00034,
            R.drawable.a00035,
            R.drawable.a00036,
            R.drawable.a00037,
            R.drawable.a00038,
            R.drawable.a00039,
            R.drawable.a00040,
            R.drawable.a00041,
            R.drawable.a00042,
            R.drawable.a00043,
            R.drawable.a00044,
            R.drawable.a00045,
            R.drawable.a00046,
            R.drawable.a00047,
            R.drawable.a00048,
            R.drawable.a00049,
            R.drawable.a00050
        )
        val bitmapList = resList.map {
            Config.BitmapSource.ResourceBitmap(it)
        }
        binding.m17AnimationLayout.post {
            binding.m17AnimationLayout.addView(
                M17AnimationTextureView(binding.root.context).apply {
                    setup(
                        binding.m17AnimationLayout.width,
                        binding.m17AnimationLayout.height,
                        bitmapList,
                        20,
                        M17AnimationTextureView.LOOP)
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    fun recycle(){
        binding.m17AnimationLayout.removeAllViews()
    }
}