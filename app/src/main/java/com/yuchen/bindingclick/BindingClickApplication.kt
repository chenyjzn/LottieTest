package com.yuchen.bindingclick

import android.app.Application
import kotlin.properties.Delegates

class BindingClickApplication : Application() {
    companion object {
        var instance: BindingClickApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
