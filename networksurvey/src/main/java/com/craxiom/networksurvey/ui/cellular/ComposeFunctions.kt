@file:JvmName("ComposeFunctions")

package com.craxiom.networksurvey.ui.cellular

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.craxiom.networksurvey.ui.theme.NsTheme

fun setContent(composeView: ComposeView, viewModel: CellularChartViewModel) {
    composeView.apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            NsTheme {
                CellularChartComponent(viewModel = viewModel)
            }
        }
    }
}