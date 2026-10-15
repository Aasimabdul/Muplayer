package com.example.muplayer.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

fun Modifier.artworkGestureDetector(
    isDoubleTapEnabled: Boolean,
    isSwipeHorizEnabled: Boolean,
    isSwipeVertEnabled: Boolean,
    isLongPressEnabled: Boolean,
    onDoubleTap: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onLongPress: () -> Unit
): Modifier = this
    .pointerInput(isDoubleTapEnabled, isLongPressEnabled) {
        detectTapGestures(
            onDoubleTap = if (isDoubleTapEnabled) { { onDoubleTap() } } else null,
            onLongPress = if (isLongPressEnabled) { { onLongPress() } } else null
        )
    }
    .pointerInput(isSwipeHorizEnabled, isSwipeVertEnabled) {
        var totalX = 0f
        var totalY = 0f
        detectDragGestures(
            onDragStart = {
                totalX = 0f
                totalY = 0f
            },
            onDrag = { change, dragAmount ->
                change.consume()
                totalX += dragAmount.x
                totalY += dragAmount.y
            },
            onDragEnd = {
                val threshold = 120f
                val absX = abs(totalX)
                val absY = abs(totalY)

                if (isSwipeHorizEnabled && absX > threshold && absX > absY * 1.3f) {
                    if (totalX < 0) onSwipeLeft() else onSwipeRight()
                } else if (isSwipeVertEnabled && absY > threshold && absY > absX * 1.3f) {
                    if (totalY < 0) onSwipeUp() else onSwipeDown()
                }
            }
        )
    }
