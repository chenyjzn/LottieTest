package com.yuchen.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Handler
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.LruCache
import com.yuchen.bindingclick.BindingClickApplication
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File

private val DEBUG = false

class BitmapCachePool {

    private val cacheBitmap = ArrayList<BitmapHolder>(5)
    private var cachePosition = 0
    private var cachedMaxSize = 4

    private var lastHolder: BitmapHolder? = null
    var failCount = 0

    private var width: Int = 0
    private var height: Int = 0
    private var preferred: Bitmap.Config = Bitmap.Config.RGB_565
    // sourceList is not thread safe, you need to care about if you are using multi-thread
    private val sourceList = mutableListOf<Config.BitmapSource>()

    fun setup(
        width: Int,
        height: Int,
        preferred: Bitmap.Config,
        source: List<Config.BitmapSource>
    ) {
        synchronized(this) {
            this.internalClear()
            this.width = width
            this.height = height
            this.preferred = preferred
            this.sourceList.clear()
            this.sourceList.addAll(source)
        }
    }

    fun drawLastHolder(block: (Boolean, BitmapHolder?) -> Unit) {
        synchronized(this) {
            lastHolder?.startDraw()
            block(lastHolder?.isValid() ?: false, lastHolder)
            lastHolder?.endDraw()
        }
    }

    fun popBitmapAndDraw(block: (Boolean, BitmapHolder?) -> Unit) {
        synchronized(this) {
            if (cacheBitmap.isEmpty()) {
                failCount++
                drawLastHolder(block)
            } else {
                cacheBitmap.removeAt(0).let { holder ->
                    lastHolder?.endCache()
                    lastHolder = holder

                    holder.startDraw()
                    block(holder.isValid(), holder)
                    holder.endDraw()
                }
            }
        }
    }

    fun prepareNextSource() {
        synchronized(this) {
            if (cachePosition >= sourceList.size) {
                cachePosition = 0
            }
            if (cacheBitmap.size < cachedMaxSize) {
                prepareSourceIndex(cachePosition)
                cachePosition++
            }
        }
    }

    /**
     * prepareNextSource in io thread
     */
    fun syncPrepareNextSource() {
        CoroutineScope(Dispatchers.IO).launch {
            prepareNextSource()
        }
    }

    fun cacheSize() = cacheBitmap.size

    fun isCacheNotEmpty() = cacheBitmap.isNotEmpty()

    fun failCacheBitmap(): Boolean = false

    private fun prepareSourceIndex(i: Int) {
        val bitmapSource = sourceList.getOrNull(i) ?: return
        val newBitmapHolder = getDecodeBitmap(bitmapSource) ?: return
        newBitmapHolder.startCache()
        cacheBitmap.add(newBitmapHolder)
    }

    private fun getDecodeBitmap(source: Config.BitmapSource): BitmapHolder? {
        return BitmapProvider.getBitmapHolder(
            Config(
                source,
                width,
                height,
                preferred
            )
        )
    }
    private fun internalClear() {
        while (cacheBitmap.isNotEmpty()) {
            cacheBitmap.removeAt(0).endCache()
        }
        lastHolder?.endCache()
        lastHolder = null
        cachePosition = 0
    }

    fun clear() {
        synchronized(this) {
            internalClear()
        }
    }
}

// change to injection
object BitmapProvider {

    val context: Context = BindingClickApplication.instance.baseContext
    val resources: Resources = context.resources

    var createdBitmapCount = 0

    private val lruCache by lazy {
        val maxHeapSize = Runtime.getRuntime().maxMemory() / 1048576L // 1MB
        var cacheSize: Int = (maxHeapSize * 0.1).toInt()
        if (cacheSize > 64) {
            cacheSize = 64
        }
        ProviderLruCache(
            cacheSize * 1024 * 1024
        )
    }

    class ProviderLruCache(cacheSize: Int) : LruCache<Config, BitmapHolder>(cacheSize) {
        override fun sizeOf(key: Config?, value: BitmapHolder): Int {
            return value.byteCount
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: Config?,
            oldValue: BitmapHolder?,
            newValue: BitmapHolder?
        ) {

            if (oldValue?.bitmap?.isRecycled == true) {

            } else {
                createdBitmapCount--
                oldValue?.bitmap?.recycle()
            }
        }
    }

    private fun getBitmapBytes(bitmap: Bitmap): Int {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            bitmap.allocationByteCount
        } else {
            bitmap.byteCount
        }
    }

    fun getBitmapHolder(config: Config): BitmapHolder? {
        synchronized(lruCache) {
            var bitmapAndConfig: BitmapHolder? = lruCache.get(config)
            if (bitmapAndConfig == null) {
                val bitmap = getDecodeBitmap(
                    config.source,
                    config.toOption()
                ) ?: return null
                bitmapAndConfig =
                    BitmapHolder(
                        bitmap,
                        config,
                        getBitmapBytes(
                            bitmap
                        )
                    )
                lruCache.put(config, bitmapAndConfig)
            }
            return bitmapAndConfig
        }
    }

    fun removeBitmap(holder: BitmapHolder) {
        synchronized(lruCache) {
            lruCache.remove(holder.config)
        }
    }

    fun getBitmap(config: Config): Bitmap? {
        return getBitmapHolder(
            config
        )?.bitmap
    }

    private fun getDecodeBitmap(
        source: Config.BitmapSource,
        options: BitmapFactory.Options,
        isDrawBmpFlip: Boolean = false
    ): Bitmap? {

        var bitmap: Bitmap? = null
        try {
            when (source) {
                is Config.BitmapSource.FileBitmap -> {
                    bitmap = if (source.filename.startsWith("assets://")) {
                        BitmapFactory.decodeStream(
                            context.assets.open(
                                source.filename.replace(
                                    "assets://",
                                    ""
                                )
                            ), null, options
                        )
                    } else {
                        BitmapFactory.decodeFile(source.filename, options)
                    }
                    if (bitmap != null) createdBitmapCount++
                }
                is Config.BitmapSource.ResourceBitmap -> {
                    bitmap = BitmapFactory.decodeResource(resources, source.resId, options)
                    if (bitmap != null) createdBitmapCount++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap?.let {
            if (isDrawBmpFlip) {
                bitmap =
                    flipBitmap(
                        it
                    )
            }
            bitmap
        }
    }

    private fun flipBitmap(src: Bitmap): Bitmap? {
        try {
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val dst = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, false)
            dst.density = DisplayMetrics.DENSITY_DEFAULT
            src.recycle()
            return dst
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return src
    }

    fun clearAllCache() {
        lruCache.evictAll()
        Handler().asCoroutineDispatcher()
    }
}

class BitmapHolder(
    val bitmap: Bitmap,
    val config: Config,
    val byteCount: Int,
    var refOnDrawCount: Int = 0,
    var refCacheCount: Int = 0
) {
    var isRecycled: Boolean = false
    fun isValid(): Boolean = !isRecycled

    /**
     * count the cache reference when start
     * */
    fun startDraw() {
        synchronized(this) {
            refOnDrawCount++
        }
    }

    /**
     * count the cache reference when end
     * */
    fun endDraw() {
        synchronized(this) {
            refOnDrawCount--
            recycleBitmapIfNeed()
        }
    }

    /**
     * count the cache reference when start
     * */
    fun startCache() {
        synchronized(this) {
            refCacheCount++
        }
    }

    /**
     * count the cache reference when end
     * */
    fun endCache() {
        synchronized(this) {
            refCacheCount--
            recycleBitmapIfNeed()
        }
    }

    private fun recycleBitmapIfNeed() {
        if (refOnDrawCount <= 0 && refCacheCount <= 0 && !isRecycled) {
            isRecycled = true
            BitmapProvider.removeBitmap(this)
        }
    }
}

data class Config(
    val source: BitmapSource,
    val width: Int,
    val height: Int,
    val preferred: Bitmap.Config
) {

    fun toOption(): BitmapFactory.Options {
        return BitmapFactory.Options().apply {
            inMutable = false
            inPreferredConfig = preferred
            inDensity = width
            inTargetDensity = height
        }
    }

    @Parcelize
    open class BitmapSource : Parcelable {
        @Parcelize
        data class ResourceBitmap(val resId: Int) : BitmapSource(), Parcelable

        @Parcelize
        data class FileBitmap(val filename: String) : BitmapSource(), Parcelable
    }
}

fun String.toSourceList(firstIndex: Int = -1, lastIndex: Int = -1): List<Config.BitmapSource> {
    return File(this).list()?.sortedWith<String>(Comparator { a, b -> a.toInt() - b.toInt() })
        ?.let {
            if (firstIndex >= 0 && lastIndex >= 0) {
                it.filterIndexed { index, _ -> (index in firstIndex..lastIndex) }
            } else {
                it
            }.map { str ->
                Config.BitmapSource.FileBitmap("$this/$str")
            }
        } ?: mutableListOf()
}

fun Context.toSourceList(
    folderName: String,
    firstIndex: Int = -1,
    lastIndex: Int = -1
): List<Config.BitmapSource> {
    val name = folderName.replaceFirst("assets://", "")
    return assets.list(name)?.sortedWith<String>(Comparator { a, b -> a.toInt() - b.toInt() })
        ?.let {
            if (firstIndex >= 0 && lastIndex >= 0) {
                it.filterIndexed { index, _ -> (index in firstIndex..lastIndex) }
            } else {
                it
            }.map { str ->
                Config.BitmapSource.FileBitmap("$folderName/$str")
            }
        } ?: mutableListOf()
}