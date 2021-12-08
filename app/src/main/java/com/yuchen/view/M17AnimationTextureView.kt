package com.yuchen.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.TextureView
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class M17AnimationTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr),
    TextureView.SurfaceTextureListener {

    companion object {
        const val LOOP = -1
    }

    private var source: List<Config.BitmapSource> = emptyList()
    private var renderScope: RenderScope = RenderScope(this)

    var sense: Sense = Sense()
    private var fps: Int = 12
    var animationListener: OnAnimationListener? = null

    var startWhenAvailable = true

    /**
     * if fix duration is true each frame duration will be 1000/fps
     * otherwise the duration will include canvas render time
     * so that if canvas draw time over the 1000/fps some frame will drop
     */
    var fixDuration = false

    init {
        surfaceTextureListener = this
        isOpaque = false
    }

    fun setup(
        viewWidth: Int,
        viewHeight: Int,
        path: String,
        fps: Int = 12,
        repeat: Int = LOOP,
        isOpaqueSource: Boolean = true,
        preferred: Bitmap.Config = Bitmap.Config.RGB_565
    ) {
        setup(viewWidth, viewHeight, path.toSourceList(), fps, repeat, isOpaqueSource, preferred)
    }

    fun setup(
        viewWidth: Int,
        viewHeight: Int,
        source: List<Config.BitmapSource>,
        fps: Int = 12,
        repeat: Int = LOOP,
        isOpaqueSource: Boolean = true,
        preferred: Bitmap.Config = Bitmap.Config.RGB_565
    ) {
        this.source = source
        this.fps = fps
        if (viewHeight == 0 || viewWidth == 0) throw IllegalAnimationSize(" viewHeight = $viewHeight or viewWidth = $viewWidth should not be zero")
        this.sense.setup(viewWidth, viewHeight, true, source, repeat, isOpaqueSource, preferred)
    }

    fun startAnimation() {
        renderScope.apply {
            if (isAvailable) {
                fps = this@M17AnimationTextureView.fps
                fixDuration = this@M17AnimationTextureView.fixDuration
                start()
            } else {
                startWhenAvailable = true
            }
        }
    }

    fun stopAnimation() {
        renderScope.stop()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderScope.apply {
            interruptStop()
        }
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        // Make first frame is TRANSPARENT or it will be black at first place
        var canvas: Canvas? = null
        try {
            canvas = lockCanvas()
            canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        } catch (e: Exception) {
        } finally {
            canvas?.let {
                unlockCanvasAndPost(it)
            }
        }
        if (startWhenAvailable) {
            startAnimation()
        }
    }

    suspend fun dispatchOnAnimationEnd() = withContext(Dispatchers.Main) {
        animationListener?.onEndAnimation()
    }

    inner class Sense {
        private val paint = Paint()

        private var time = System.currentTimeMillis()
        private var renderFrameCount = 0
        private var frameCount = 0
        private var repeatCount = 0
        private var repeat: Int = LOOP
        var bitmapPool: BitmapCachePool = BitmapCachePool()
        private var isOpaqueSource: Boolean = true

        fun start() {
            this.time = System.currentTimeMillis()
            this.repeatCount = 0
            this.renderFrameCount = 0
        }

        fun setup(
            width: Int,
            height: Int,
            fitCenterCrop: Boolean,
            source: List<Config.BitmapSource>,
            repeat: Int,
            isOpaqueSource: Boolean,
            preferred: Bitmap.Config
        ) {
            this.isOpaqueSource = isOpaqueSource
            this.repeat = repeat
            bitmapPool.apply {
                setup(width, height, preferred, source)
                prepareNextSource()
            }
        }

        suspend fun onDraw(canvas: Canvas) {

            renderFrameCount++
            if (System.currentTimeMillis() - time >= 1000L) {
                renderFrameCount = 0
                time = System.currentTimeMillis()
            }

            val drawBlock: (Boolean, BitmapHolder?) -> Unit = { success, holder ->
                if (holder != null) {
                    if (success) {
                        // draw full screen color will take 0-3 ms
                        if (isOpaqueSource) canvas.drawColor(
                            Color.TRANSPARENT,
                            PorterDuff.Mode.CLEAR
                        )

                        val time = measureTimeMillis {
                            canvas.drawBitmap(
                                holder.bitmap,
                                Rect(0, 0, holder.bitmap.width, holder.bitmap.height),
                                Rect(0, 0, holder.config.width, holder.config.height),
                                paint
                            )
                        }
                        // LogDog.d("drawBitmap time = $time width= ${holder.bitmap.width} height=${holder.bitmap.height} config.width= ${holder.config.width} config.height=${holder.config.height} path=${holder.config.source}")
                    } else {
                    }
                } else {
                }
            }

            if (frameCount >= source.size) {
                frameCount = 0
                repeatCount++
            }

            if (repeat != LOOP && repeatCount >= repeat) {
                bitmapPool.drawLastHolder(drawBlock)
                dispatchOnAnimationEnd()
                stopAnimation()
            } else {
                bitmapPool.popBitmapAndDraw(drawBlock)
                frameCount++
            }
        }

        fun release() {
            bitmapPool.clear()
        }
    }

    class RenderScope(
        private val textureView: M17AnimationTextureView,
        val supervisorJob: CompletableJob = SupervisorJob()
    ) : CoroutineScope by CoroutineScope(Dispatchers.Default + supervisorJob) {

        var fps: Int = 12
        var isRunning: Boolean = false
        var fixDuration: Boolean = false
        private var previousTime: Long = 0
        private var job: Job? = null

        fun start() {
            job = launch {
                isRunning = true
                textureView.sense.bitmapPool.prepareNextSource()
                textureView.sense.start()
                while (isRunning) {
                    previousTime = System.currentTimeMillis()
                    var canvas: Canvas? = null
                    try {
                        canvas = textureView.lockCanvas()
                        if (canvas == null) {
                            delay(1)
                            continue
                        }
                        textureView.sense.onDraw(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (canvas != null) {
                            textureView.unlockCanvasAndPost(canvas)
                        }
                    }
                    if (!isRunning) break
                    textureView.sense.bitmapPool.syncPrepareNextSource()
                    val renderTime = System.currentTimeMillis() - previousTime
                    val sleepTimeMs = if (fixDuration) (1000L / fps)
                    else (1000L / fps - renderTime)
                    if (sleepTimeMs > 5L) {
                        delay(sleepTimeMs)
                    } else {
                        delay(5L)
                    }
                }
            }
        }

        fun interruptStop() {
            isRunning = false
            runBlocking {
                job?.cancelAndJoin()
            }
            launch {
                textureView.sense.release()
            }
        }

        fun stop() {
            isRunning = false
            launch {
                textureView.sense.release()
            }
        }
    }

    interface OnAnimationListener {
        fun onEndAnimation()
    }
}

class IllegalAnimationSize(override val message: String) : IllegalArgumentException(message)