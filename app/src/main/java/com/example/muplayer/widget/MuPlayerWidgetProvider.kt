package com.example.muplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.RemoteViews
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.muplayer.MainActivity
import com.example.muplayer.R
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.core.graphics.toColorInt
import androidx.palette.graphics.Palette
import com.example.muplayer.service.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.Locale

open class MuPlayerWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.muplayer.widget.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.muplayer.widget.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.muplayer.widget.ACTION_PREVIOUS"

        var cachedTitle: String = "MuPlayer"
        var cachedArtist: String = "Tap to play music"
        var cachedAlbum: String = "Unknown Album"
        var cachedIsPlaying: Boolean = false
        var cachedProgressMs: Long = 0L
        var cachedDurationMs: Long = 0L
        var cachedBitmap: Bitmap? = null
        var cachedAmbientColor: Int = "#F20D1828".toColorInt()

        fun updateAllWidgets(
            context: Context,
            title: String,
            artist: String,
            album: String,
            isPlaying: Boolean,
            progressMs: Long,
            durationMs: Long,
            artworkUri: Uri?
        ) {
            cachedTitle = if (title.isBlank()) "MuPlayer" else title
            cachedArtist = if (artist.isBlank()) "Tap to play music" else artist
            cachedAlbum = if (album.isBlank()) "Unknown Album" else album
            cachedIsPlaying = isPlaying
            cachedProgressMs = progressMs.coerceAtLeast(0L)
            cachedDurationMs = durationMs.coerceAtLeast(0L)

            CoroutineScope(Dispatchers.IO).launch {
                if (artworkUri != null) {
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(artworkUri)
                        val fullBitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        if (fullBitmap != null) {
                            cachedBitmap = Bitmap.createScaledBitmap(fullBitmap, 200, 200, true)
                            val palette = Palette.from(cachedBitmap!!).generate()
                            val rawColor = palette.getMutedColor("#0D1828".toColorInt())
                            // Apply 90% opacity
                            cachedAmbientColor = Color.argb(
                                230,
                                Color.red(rawColor),
                                Color.green(rawColor),
                                Color.blue(rawColor)
                            )
                        } else {
                            cachedBitmap = null
                            cachedAmbientColor = "#F20D1828".toColorInt()
                        }
                    } catch (_: Exception) {
                        cachedBitmap = null
                        cachedAmbientColor = "#F20D1828".toColorInt()
                    }
                } else {
                    cachedBitmap = null
                    cachedAmbientColor = "#F20D1828".toColorInt()
                }

                val appWidgetManager = AppWidgetManager.getInstance(context)

                updateProviderWidgets(context, appWidgetManager, MuPlayerWidgetCompactProvider::class.java, R.layout.widget_compact_2x2)
                updateProviderWidgets(context, appWidgetManager, MuPlayerWidgetWideProvider::class.java, R.layout.widget_wide_4x1)
                updateProviderWidgets(context, appWidgetManager, MuPlayerWidgetExpandedProvider::class.java, R.layout.widget_expanded_4x2)
            }
        }

        private fun updateProviderWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            providerClass: Class<*>,
            layoutId: Int
        ) {
            val componentName = ComponentName(context, providerClass)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val views = buildRemoteViews(context, providerClass, layoutId)
                appWidgetManager.updateAppWidget(appWidgetIds, views)
            }
        }

        fun buildRemoteViews(context: Context, providerClass: Class<*>, layoutId: Int): RemoteViews {
            val views = RemoteViews(context.packageName, layoutId)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setColorStateList(R.id.widget_root, "setBackgroundTintList", ColorStateList.valueOf(cachedAmbientColor))
            } else {
                views.setInt(R.id.widget_root, "setBackgroundColor", cachedAmbientColor)
            }

            views.setTextViewText(R.id.widget_title, cachedTitle)
            views.setTextViewText(R.id.widget_artist, cachedArtist)

            if (layoutId == R.layout.widget_expanded_4x2) {
                views.setTextViewText(R.id.widget_album, cachedAlbum)

                val maxProg = if (cachedDurationMs > 0L) 100 else 0
                val curProg = if (cachedDurationMs > 0L) ((cachedProgressMs * 100) / cachedDurationMs).toInt() else 0
                views.setProgressBar(R.id.widget_progress_bar, maxProg, curProg, false)

                val elapsedStr = formatTime(cachedProgressMs)
                val durationStr = formatTime(cachedDurationMs)
                views.setTextViewText(R.id.widget_time_text, "$elapsedStr / $durationStr")
            }

            if (cachedBitmap != null) {
                views.setImageViewBitmap(R.id.widget_album_art, cachedBitmap)
            } else {
                views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_widget_album_placeholder)
            }

            val playPauseIcon = if (cachedIsPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            views.setImageViewResource(R.id.btn_widget_play_pause, playPauseIcon)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_now_playing", true)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            views.setOnClickPendingIntent(R.id.btn_widget_prev, getPendingIntent(context, providerClass, ACTION_PREVIOUS, 1))
            views.setOnClickPendingIntent(R.id.btn_widget_play_pause, getPendingIntent(context, providerClass, ACTION_PLAY_PAUSE, 2))
            views.setOnClickPendingIntent(R.id.btn_widget_next, getPendingIntent(context, providerClass, ACTION_NEXT, 3))

            return views
        }

        private fun getPendingIntent(context: Context, providerClass: Class<*>, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, providerClass).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun formatTime(timeMs: Long): String {
            val totalSeconds = (timeMs / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            val layoutId = getLayoutId()
            val views = buildRemoteViews(context, this.javaClass, layoutId)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == ACTION_PLAY_PAUSE || action == ACTION_NEXT || action == ACTION_PREVIOUS) {
            handleMediaAction(context, action)
        }
    }

    private fun handleMediaAction(context: Context, action: String) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                when (action) {
                    ACTION_PLAY_PAUSE -> {
                        if (controller.isPlaying) controller.pause() else controller.play()
                    }
                    ACTION_NEXT -> controller.seekToNextMediaItem()
                    ACTION_PREVIOUS -> controller.seekToPreviousMediaItem()
                }
            } catch (_: Exception) {}
            MediaController.releaseFuture(controllerFuture)
        }, MoreExecutors.directExecutor())
    }

    open fun getLayoutId(): Int = R.layout.widget_wide_4x1
}

class MuPlayerWidgetCompactProvider : MuPlayerWidgetProvider() {
    override fun getLayoutId(): Int = R.layout.widget_compact_2x2
}

class MuPlayerWidgetWideProvider : MuPlayerWidgetProvider() {
    override fun getLayoutId(): Int = R.layout.widget_wide_4x1
}

class MuPlayerWidgetExpandedProvider : MuPlayerWidgetProvider() {
    override fun getLayoutId(): Int = R.layout.widget_expanded_4x2
}
