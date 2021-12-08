package com.yuchen.bindingclick

import android.R.attr.data
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.yuchen.bindingclick.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        bindingseekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                binding.mask.setPercentage(progress.toFloat()/100f)
//                binding.dailyProgress2.setProgress(progress.toFloat()/100f)
//                binding.dailyProgress.setCount(progress/20,5)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//
//            }
//        })
//
//        binding.dailyProgress2.setPointCount(3)
//        binding.dailyProgress.setCount(0,5)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 5)
        val adapter = LottieTestAdapter()
        binding.recyclerView.adapter = adapter
//        adapter.notifyDataSetChanged()
        setContentView(binding.root)
    }
}