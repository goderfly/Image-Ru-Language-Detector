package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberDialogState
import utils.StorableInt

data class DialogEntity(
    val isShowed: MutableState<Boolean> = mutableStateOf(false),
    var title: MutableState<String> = mutableStateOf("Информация"),
)

@ExperimentalMaterialApi
@Preview
@Composable
fun showAlertDialog(showDialog: MutableState<DialogEntity>) {
    if (showDialog.value.isShowed.value) {
        Dialog(
            state = rememberDialogState(size = WindowSize(390.dp, 125.dp)),
            title = showDialog.value.title.value,
            onCloseRequest = { showDialog.value.isShowed.value = false },
            content = { dialogContent() }
        )
    }
}

@Composable
private fun dialogContent() {
    val textState = remember { mutableStateOf(TextFieldValue()) }
    var pageDeep by StorableInt()
    val pageDeepState = remember { mutableStateOf(pageDeep) }

    Row(Modifier.padding(16.dp)) {
        TextField(
            modifier = Modifier.width(270.dp),
            label = { Text("Тек. кол-во. страниц поиска: ${pageDeepState.value}") },
            value = textState.value,
            onValueChange = { textState.value = it }
        )
        Button(
            modifier = Modifier
                .weight(1F)
                .height(55.dp)
                .absolutePadding(left = 8.dp),
            onClick = {
                pageDeep = textState.value.text.toInt()
                pageDeepState.value = textState.value.text.toInt()
            }
        ) {
            Text("\uD83D\uDCBE")
        }
    }
}


