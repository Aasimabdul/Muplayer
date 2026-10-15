package com.example.muplayer.ui.components

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.muplayer.model.AudioTrack
import com.example.muplayer.repository.TagEditorManager
import com.example.muplayer.repository.TagSaveResult
import com.example.muplayer.repository.TrackTagsData
import com.example.muplayer.ui.theme.uniqueLiquidDisplay
import com.example.muplayer.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagEditorDialog(
    track: AudioTrack,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tagManager = remember { TagEditorManager(context) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist) }
    var album by remember { mutableStateOf(track.album) }
    var albumArtist by remember { mutableStateOf("") }
    var trackNumber by remember { mutableStateOf("") }
    var discNumber by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    var customArtBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var removeArtwork by remember { mutableStateOf(false) }
    var pendingTagsToSave by remember { mutableStateOf<TrackTagsData?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    customArtBitmap = bitmap
                    removeArtwork = false
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Failed to load selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingTagsToSave?.let { tags ->
                coroutineScope.launch {
                    isSaving = true
                    when (val res = tagManager.saveTags(tags)) {
                        is TagSaveResult.Success -> {
                            Toast.makeText(context, "Tags updated successfully!", Toast.LENGTH_SHORT).show()
                            viewModel.loadLibrary()
                            viewModel.refreshCurrentTrackMetadata(tags)
                            onDismiss()
                        }
                        is TagSaveResult.Error -> {
                            Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                    isSaving = false
                }
            }
        } else {
            Toast.makeText(context, "Permission denied to edit tags", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(track) {
        isLoading = true
        val tags = tagManager.readTags(track)
        title = tags.title
        artist = tags.artist
        album = tags.album
        albumArtist = tags.albumArtist
        trackNumber = tags.trackNumber
        discNumber = tags.discNumber
        genre = tags.genre
        year = tags.year
        isLoading = false
    }

    fun performSave() {
        if (title.isBlank()) {
            Toast.makeText(context, "Song title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val tagsToSave = TrackTagsData(
            trackId = track.id,
            uri = track.uri,
            title = title,
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            trackNumber = trackNumber,
            discNumber = discNumber,
            genre = genre,
            year = year,
            albumArtUri = track.albumArtUri,
            customArtBitmap = customArtBitmap,
            removeArtwork = removeArtwork
        )
        pendingTagsToSave = tagsToSave

        coroutineScope.launch {
            isSaving = true
            when (val res = tagManager.saveTags(tagsToSave)) {
                is TagSaveResult.Success -> {
                    Toast.makeText(context, "Tags updated successfully!", Toast.LENGTH_SHORT).show()
                    viewModel.loadLibrary()
                    viewModel.refreshCurrentTrackMetadata(tagsToSave)
                    onDismiss()
                }
                is TagSaveResult.PermissionRequired -> {
                    val request = IntentSenderRequest.Builder(res.intentSender).build()
                    intentSenderLauncher.launch(request)
                }
                is TagSaveResult.Error -> {
                    Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                }
            }
            isSaving = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .uniqueLiquidDisplay(
                    shape = RoundedCornerShape(28.dp),
                    accentColor = MaterialTheme.colorScheme.primary
                )
                .padding(20.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Edit Song Metadata",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (customArtBitmap != null) {
                                        Image(
                                            bitmap = customArtBitmap!!.asImageBitmap(),
                                            contentDescription = "Custom Artwork",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (!removeArtwork && track.albumArtUri != null) {
                                        AsyncImage(
                                            model = track.albumArtUri,
                                            contentDescription = "Album Art",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.MusicNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Choose Artwork")
                                }

                                if (customArtBitmap != null || (!removeArtwork && track.albumArtUri != null)) {
                                    TextButton(
                                        onClick = {
                                            customArtBitmap = null
                                            removeArtwork = true
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Remove Artwork")
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = artist,
                            onValueChange = { artist = it },
                            label = { Text("Artist") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = album,
                            onValueChange = { album = it },
                            label = { Text("Album") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = albumArtist,
                            onValueChange = { albumArtist = it },
                            label = { Text("Album Artist") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = trackNumber,
                                onValueChange = { trackNumber = it },
                                label = { Text("Track #") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = discNumber,
                                onValueChange = { discNumber = it },
                                label = { Text("Disc #") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = genre,
                                onValueChange = { genre = it },
                                label = { Text("Genre") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = year,
                                onValueChange = { year = it },
                                label = { Text("Year") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            enabled = !isSaving
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { performSave() },
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save Tags")
                            }
                        }
                    }
                }
            }
        }
    }
}
