import androidx.media3.session.CommandButton
import androidx.media3.common.Player
val customLayout = listOf(
    CommandButton.Builder().setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS).setIconResId(androidx.media3.ui.R.drawable.exo_icon_previous).setDisplayName("Previous").build()
)
