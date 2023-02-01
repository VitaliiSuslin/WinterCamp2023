@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wintercamp.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wintercamp.Injector
import com.example.wintercamp.MyApplication
import com.example.wintercamp.location.service.LocationService
import com.example.wintercamp.ui.theme.WinterCampTheme
import com.example.wintercamp.viewmodel.MainUiAction
import com.example.wintercamp.viewmodel.MainUiEvent
import com.example.wintercamp.viewmodel.MainViewModel
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WinterCampTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainUi()
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun MainUi(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel()
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            LocationSection()
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                onClick = {
                    viewModel.processAction(MainUiAction.StartService)
                }) {
                Text(text = "Start service")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                onClick = {
                    viewModel.processAction(MainUiAction.StopService)
                }) {
                Text(text = "Stop service")
            }
//            RebootOption()
        }
        ObserveAction(viewModel.action)
    }
}

@Composable
private fun LocationSection(modifier: Modifier = Modifier) {
    val appProcessor = Injector.locationProcessor
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (appProcessor.locationStatusState.collectAsState().value) {
                LocationService.LocationStatus.FOUND -> Image(
                    painter = rememberVectorPainter(image = Icons.Default.LocationOn),
                    contentDescription = "Location found",
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                )
                LocationService.LocationStatus.LOST -> Image(
                    painter = rememberVectorPainter(image = Icons.Default.Warning),
                    contentDescription = "List satellite",
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                )
            }
        }
        Text(
            text = "Current location",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            val location = appProcessor.locationState.collectAsState(initial = null).value

            Text(
                text = "Longitude: ${location?.longitude ?: 0}",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
            Text(
                text = "Latitude: ${location?.latitude ?: 0}",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun RebootOption() {
    val viewModel: MainViewModel = viewModel()
    val activity = (LocalLifecycleOwner.current as Activity)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            val isChecked = viewModel.rebootOption.collectAsState().value
            Checkbox(checked = isChecked, onCheckedChange = {
                if (it != isChecked) {
                    viewModel.processUiEvent(MainUiEvent.SetRebootState(it))
                }
            })
            Text(text = "Set reboot option")
        }

        Button(
            onClick = {
                //Make crash error
                activity.finishAndRemoveTask()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
        ) {
            Text(text = "Finish and restart service")
        }
    }
}

@Composable
private fun ObserveAction(actionState: StateFlow<MainUiAction>) {
    when (actionState.collectAsState().value) {
        MainUiAction.StartService -> {
            (LocalContext.current.applicationContext as MyApplication).let {
                LocationService.start(it)
            }
        }
        MainUiAction.StopService -> {
            (LocalContext.current.applicationContext as MyApplication).let {
                LocationService.stop(it)
            }
        }
        MainUiAction.Empty -> {
            //nothing to do
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WinterCampTheme {
        MainUi()
    }
}