package com.example.muplayer.service

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList

@OptIn(UnstableApi::class)
class MuPlayerNotificationProvider(context: Context) : DefaultMediaNotificationProvider(context) {

    override fun getMediaButtons(
        session: MediaSession,
        playerCommands: Player.Commands,
        customLayout: ImmutableList<CommandButton>,
        showPauseButton: Boolean
    ): ImmutableList<CommandButton> {
        val repeatMode = session.player.repeatMode
        val repeatIcon = when (repeatMode) {
            Player.REPEAT_MODE_ONE -> androidx.media3.ui.R.drawable.exo_icon_repeat_one
            Player.REPEAT_MODE_ALL -> androidx.media3.ui.R.drawable.exo_icon_repeat_all
            else -> androidx.media3.ui.R.drawable.exo_icon_repeat_off
        }

        val repeatButton = CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_SET_REPEAT_MODE)
            .setIconResId(repeatIcon)
            .setDisplayName("Repeat")
            .build()
            
        val prevButton = CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS)
            .setIconResId(androidx.media3.ui.R.drawable.exo_icon_previous)
            .setDisplayName("Previous")
            .build()
            
        val playPauseButton = CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
            .setIconResId(if (showPauseButton) androidx.media3.ui.R.drawable.exo_icon_pause else androidx.media3.ui.R.drawable.exo_icon_play)
            .setDisplayName(if (showPauseButton) "Pause" else "Play")
            .build()
            
        val nextButton = CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT)
            .setIconResId(androidx.media3.ui.R.drawable.exo_icon_next)
            .setDisplayName("Next")
            .build()

        return ImmutableList.of(repeatButton, prevButton, playPauseButton, nextButton)
    }
}
