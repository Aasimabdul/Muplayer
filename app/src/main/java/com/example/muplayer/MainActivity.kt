package com.example.muplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.muplayer.ui.screens.MainScreen
import com.example.muplayer.ui.theme.MuPlayerTheme
import com.example.muplayer.viewmodel.MusicViewModel
import com.example.muplayer.R

class MainActivity : ComponentActivity() {

    private var permissionGranted by mutableStateOf(false)

    private val multiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionGranted = permissions[audioPermission] == true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        
        setContent {
            val viewModel: MusicViewModel = viewModel()
            val theme by viewModel.currentTheme.collectAsState()
            
            MuPlayerTheme(themeName = theme) {
                MainScreen(
                    viewModel = viewModel,
                    hasPermission = permissionGranted,
                    onRequestPermission = { checkAndRequestPermissions() }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (checkSelfPermission(audioPermission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
        } else {
            permissionsToRequest.add(audioPermission)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun LauncherIconPreview() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.size(108.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        )
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        )
    }
}
