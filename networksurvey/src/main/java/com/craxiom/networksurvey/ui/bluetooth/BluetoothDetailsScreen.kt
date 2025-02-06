package com.craxiom.networksurvey.ui.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.craxiom.networksurvey.R
import com.craxiom.networksurvey.constants.BluetoothMessageConstants
import com.craxiom.networksurvey.fragments.BluetoothDetailsFragment
import com.craxiom.networksurvey.ui.SignalChart
import com.craxiom.networksurvey.ui.UNKNOWN_RSSI
import com.craxiom.networksurvey.ui.main.appbar.TitleBar
import com.craxiom.networksurvey.util.ColorUtils

/**
 * A Compose screen that shows the details of a single Bluetooth device. The main purpose for this
 * screen is to display the RSSI chart for the selected BT device so that the RSSI can be viewed
 * over time.
 */
@Composable
internal fun BluetoothDetailsScreen(
    viewModel: BluetoothDetailsViewModel,
    bluetoothDetailsFragment: BluetoothDetailsFragment
) {
    val context = LocalContext.current
    val rssi by viewModel.rssiFlow.collectAsStateWithLifecycle()
    val scanRate by viewModel.scanRate.collectAsStateWithLifecycle()
    val colorId = ColorUtils.getColorForSignalStrength(rssi)
    val colorResource = Color(context.getColor(colorId))

    Scaffold(
        topBar = {
            TitleBar(
                "Bluetooth Device Details"
            ) { bluetoothDetailsFragment.navigateBack() }
        },
    ) { insetPadding ->
        LazyColumn(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(padding),
            verticalArrangement = Arrangement.spacedBy(padding),
            modifier = Modifier.padding(insetPadding)
        ) {
            chartItems(viewModel, colorResource, rssi, scanRate, bluetoothDetailsFragment)
        }
    }
}

private fun LazyListScope.chartItems(
    viewModel: BluetoothDetailsViewModel,
    signalStrengthColor: Color,
    rssi: Float,
    scanRate: Int,
    bluetoothDetailsFragment: BluetoothDetailsFragment
) {
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SelectionContainer {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = padding / 2)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = viewModel.bluetoothData.sourceAddress,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(
                                        LocalContext.current.getColor(R.color.colorAccent)
                                    )
                                )
                            )
                            Text(
                                text = "Source Address",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (rssi == UNKNOWN_RSSI) "Unknown" else "${rssi.toInt()} dBm",
                                style = MaterialTheme.typography.titleMedium.copy(color = signalStrengthColor)
                            )
                            Text(
                                text = "Signal Strength",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    if (viewModel.bluetoothData.otaDeviceName.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = padding, vertical = padding / 2)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Device Name: ",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = viewModel.bluetoothData.otaDeviceName,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(start = padding, end = padding, bottom = padding / 2)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Supported Technologies: ",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = BluetoothMessageConstants.getSupportedTechString(
                                viewModel.bluetoothData.supportedTechnologies
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    item {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = padding)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Scan Rate: ",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "$scanRate seconds",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                ScanRateInfoButton()

                OpenSettingsButton(bluetoothDetailsFragment)
            }
        }
    }

    cardItem {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Signal Strength (Last 2 Minutes)",
                style = MaterialTheme.typography.titleMedium
            )
            SignalChart(viewModel)
        }
    }
}


private fun LazyListScope.cardItem(content: @Composable () -> Unit) {
    item {
        Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.elevatedCardColors()) {
            Box(Modifier.padding(padding)) {
                content()
            }
        }
    }
}

@Composable
fun ScanRateInfoButton() {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }) {
        Icon(
            Icons.Default.Info,
            contentDescription = "About Bluetooth Scan Rate",
        )
    }

    // Info Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Bluetooth Scan Rate Info") },
            text = {
                Text(
                    "The rate at which Bluetooth devices will be scanned for in " +
                            "seconds. Smaller values will decrease battery life but larger values will " +
                            "cause the Signal Strength Graph to be out of date. If you want values " +
                            "closer to real time then set the scan rate to 4 seconds or less."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
fun OpenSettingsButton(bluetoothDetailsFragment: BluetoothDetailsFragment) {

    IconButton(onClick = {
        bluetoothDetailsFragment.navigateToSettings()
    }) {
        Icon(
            Icons.Default.Settings,
            contentDescription = "Settings Button",
        )
    }
}

private val padding = 16.dp
