package com.craxiom.networksurvey.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.craxiom.messaging.BluetoothRecordData
import com.craxiom.networksurvey.databinding.ContainerBluetoothDetailsFragmentBinding
import com.craxiom.networksurvey.databinding.ContainerGrpcFragmentBinding
import com.craxiom.networksurvey.databinding.ContainerMqttFragmentBinding
import com.craxiom.networksurvey.databinding.ContainerMqttQrCodeScannerFragmentBinding
import com.craxiom.networksurvey.databinding.ContainerMqttQrCodeShareFragmentBinding
import com.craxiom.networksurvey.databinding.ContainerSettingsFragmentBinding
import com.craxiom.networksurvey.databinding.ContainerTowerMapFragmentBinding
import com.craxiom.networksurvey.databinding.ContainerWifiDetailsFragmentBinding
import com.craxiom.networksurvey.databinding.ContainerWifiSpectrumFragmentBinding
import com.craxiom.networksurvey.fragments.BLUETOOTH_DATA_KEY
import com.craxiom.networksurvey.fragments.BluetoothDetailsFragment
import com.craxiom.networksurvey.fragments.MqttFragment
import com.craxiom.networksurvey.fragments.WifiDetailsFragment
import com.craxiom.networksurvey.fragments.WifiSpectrumFragment
import com.craxiom.networksurvey.fragments.model.MqttConnectionSettings
import com.craxiom.networksurvey.model.WifiNetwork
import com.craxiom.networksurvey.ui.cellular.CalculatorScreen
import com.craxiom.networksurvey.ui.wifi.model.WifiNetworkInfoList

fun NavGraphBuilder.mainGraph(
    drawerState: DrawerState,
    paddingValues: PaddingValues,
    mainNavController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    navigation(startDestination = NavDrawerOption.None.name, route = NavRoutes.MainRoute.name) {
        // TODO Need to add a header like the old display for all of these
        composable(NavDrawerOption.None.name) {
            HomeScreen(drawerState, mainNavController = mainNavController)
        }

        composable(NavDrawerOption.ServerConnection.name) {
            GrpcFragmentInCompose(paddingValues)
        }

        composable(NavDrawerOption.MqttBrokerConnection.name)
        {
            val mqttConnectionSettings =
                mainNavController.previousBackStackEntry?.savedStateHandle?.get<MqttConnectionSettings>(
                    MqttConnectionSettings.KEY
                )

            MqttFragmentInCompose(
                paddingValues = paddingValues,
                mqttConnectionSettings = mqttConnectionSettings
            )
        }

        composable(NavDrawerOption.CellularCalculators.name) {
            Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
                CalculatorScreen(viewModel = viewModel())
            }
        }

        composable(NavDrawerOption.Settings.name) {
            SettingsFragmentInCompose(paddingValues)
        }

        // --------- Deeper navigation (beyond the nav drawer) --------- //

        composable(NavOption.QrCodeScanner.name) {
            QrCodeScannerInCompose(paddingValues)
        }

        composable(NavOption.QrCodeShare.name) {
            QrCodeShareInCompose(paddingValues)
        }

        composable(NavOption.TowerMap.name) {
            TowerMapInCompose(paddingValues)
        }

        composable(NavOption.WifiSpectrum.name) {
            WifiSpectrumInCompose(paddingValues, sharedViewModel.wifiNetworkList)
        }

        composable(NavOption.WifiDetails.name) {
            val wifiNetwork =
                mainNavController.previousBackStackEntry?.savedStateHandle?.get<WifiNetwork>(
                    WifiNetwork.KEY
                )

            WifiDetailsInCompose(paddingValues, wifiNetwork)
        }

        composable(NavOption.BluetoothDetails.name) {
            val bluetoothRecordData =
                mainNavController.previousBackStackEntry?.savedStateHandle?.get<BluetoothRecordData>(
                    BLUETOOTH_DATA_KEY
                )

            BluetoothDetailsInCompose(paddingValues, bluetoothRecordData)
        }
    }
}

enum class NavDrawerOption {
    None,
    ServerConnection,
    MqttBrokerConnection,
    CellularCalculators,
    Settings,

    // External Links
    UserManual,
    MessagingDocs,
    ReportAnIssue,
    GitHub
}

enum class NavOption {
    QrCodeScanner,
    QrCodeShare,
    TowerMap,
    WifiSpectrum,
    WifiDetails,
    BluetoothDetails
}

@Composable
fun GrpcFragmentInCompose(paddingValues: PaddingValues) {
    AndroidViewBinding(
        ContainerGrpcFragmentBinding::inflate,
        modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
    }
}

@Composable
fun MqttFragmentInCompose(
    paddingValues: PaddingValues,
    mqttConnectionSettings: MqttConnectionSettings?
) {
    AndroidViewBinding(
        ContainerMqttFragmentBinding::inflate,
        modifier = Modifier.padding(paddingValues)
    ) {
        val fragment = mqttFragmentContainerView.getFragment<MqttFragment>()
        fragment.setMqttConnectionSettings(mqttConnectionSettings)
    }
}

@Composable
fun SettingsFragmentInCompose(paddingValues: PaddingValues) {
    AndroidViewBinding(
        ContainerSettingsFragmentBinding::inflate,
        modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
    }
}

@Composable
fun QrCodeScannerInCompose(paddingValues: PaddingValues) {
    AndroidViewBinding(
        ContainerMqttQrCodeScannerFragmentBinding::inflate,
        modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
    }
}

@Composable
fun QrCodeShareInCompose(paddingValues: PaddingValues) {
    AndroidViewBinding(
        ContainerMqttQrCodeShareFragmentBinding::inflate,
        modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
    }
}

@Composable
fun TowerMapInCompose(paddingValues: PaddingValues) {
    AndroidViewBinding(
        ContainerTowerMapFragmentBinding::inflate,
        //modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
    }
}

@Composable
fun WifiSpectrumInCompose(paddingValues: PaddingValues, wifiNetworks: WifiNetworkInfoList?) {
    AndroidViewBinding(
        ContainerWifiSpectrumFragmentBinding::inflate,
        modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
        if (wifiNetworks != null) {
            val fragment = wifiSpectrumFragmentContainerView.getFragment<WifiSpectrumFragment>()
            fragment.setWifiNetworks(wifiNetworks)
        }
    }
}

@Composable
fun WifiDetailsInCompose(paddingValues: PaddingValues, wifiNetwork: WifiNetwork?) {
    if (wifiNetwork != null) {
        AndroidViewBinding(
            ContainerWifiDetailsFragmentBinding::inflate,
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {
            val fragment = wifiDetailsFragmentContainerView.getFragment<WifiDetailsFragment>()
            fragment.setWifiNetwork(wifiNetwork)
        }
    }
}

@Composable
fun BluetoothDetailsInCompose(
    paddingValues: PaddingValues,
    bluetoothRecordData: BluetoothRecordData?
) {
    if (bluetoothRecordData != null) {
        AndroidViewBinding(
            ContainerBluetoothDetailsFragmentBinding::inflate,
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {
            val fragment =
                bluetoothDetailsFragmentContainerView.getFragment<BluetoothDetailsFragment>()
            fragment.setBluetoothData(bluetoothRecordData)
        }
    }
}
