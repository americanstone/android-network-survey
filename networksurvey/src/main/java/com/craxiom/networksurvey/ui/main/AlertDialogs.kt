package com.craxiom.networksurvey.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.craxiom.networksurvey.Application
import com.craxiom.networksurvey.R
import com.craxiom.networksurvey.model.GnssType
import com.craxiom.networksurvey.util.PreferenceUtils

@Composable
fun GnssFailureDialog(onDismiss: () -> Unit, onConfirm: (Boolean) -> Unit) {
    val checkedState = remember { mutableStateOf(false) }

    // TODO Validate that this dialog works
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(checkedState.value) }
            ) { Text("OK") }
        },
        text = {
            Column {
                Text("GNSS Failure")
                Checkbox(
                    checked = checkedState.value,
                    onCheckedChange = { checkedState.value = it }
                )
            }
        }
    )
}

@Composable
fun GnssFilterDialog(
    initialItems: Array<String>,
    initialChecks: BooleanArray,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current

    val selectedChecks = remember { mutableStateListOf(*initialChecks.toTypedArray()) }


    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = context.getString(R.string.filter_dialog_title)) },
        text = {
            Column {
                initialItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Toggle the checkbox state when the row is clicked
                                selectedChecks[index] = !selectedChecks[index]
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedChecks[index],
                            onCheckedChange = { isChecked ->
                                selectedChecks[index] = isChecked
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Save selections to SharedPreferences using PreferenceUtils
                    val selectedGnssTypes = GnssType.entries.toTypedArray()
                        .filterIndexed { index, _ -> selectedChecks[index] }
                        .toSet()
                    PreferenceUtils.saveGnssFilter(
                        Application.get(),
                        selectedGnssTypes,
                        Application.getPrefs()
                    )
                    onSave()
                }
            ) {
                Text(text = context.getString(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = context.getString(android.R.string.cancel))
            }
        }
    )
}

