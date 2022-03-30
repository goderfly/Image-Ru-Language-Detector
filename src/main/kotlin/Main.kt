// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.skija.Image
import ui.DialogEntity
import ui.showAlertDialog
import utils.setPickFolderJFileChooser
import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager


fun main() = application {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    Window(
        title = "Image Language Detector",
        state = WindowState(width = 400.dp, height = 270.dp),
        onCloseRequest = ::exitApplication
    ) {
        menuBar()
        App()
    }
}


@Composable
@Preview
fun App() {
    var captchaUrl by remember { mutableStateOf("") }
    var captchaDialog by remember { mutableStateOf(false) }

    if (captchaDialog) {
        Dialog(onCloseRequest = { captchaDialog = false }) {
            CaptchaDialog(captchaUrl)
        }
    }

    FetchImageUrlsInteractor.getCaptchaListener { url: String ->
        println("Обработали в main")
        captchaUrl = url
        captchaDialog = true
    }

    DesktopMaterialTheme {
        SearchWindow()
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
private fun FrameWindowScope.menuBar() {

    val alertDialog = remember { mutableStateOf(DialogEntity()) }

    MenuBar {
        Menu("Настройки") {

            Item(
                text = "Глубина поиска",
                onClick = {
                    alertDialog.value.apply {
                        title.value = "Глубина поиска"
                        isShowed.value = true
                    }
                },
                shortcut = KeyShortcut(Key.C, ctrl = true)
            )
            Item(
                text = "Путь сохранения",
                onClick = {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()).apply {
                        UIManager.put("FileChooser.saveButtonText", "Выбрать");
                        UIManager.put("FileChooser.cancelButtonText", "Отмена");
                        UIManager.put("FileChooser.saveInLabelText", "");
                        UIManager.put("FileChooser.usesSingleFilePane", true);
                        JFileChooser(Configuration.savePath).apply {
                            components.setPickFolderJFileChooser()
                            dialogTitle = "Выберите папку для сохранения изображений"
                            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                            isAcceptAllFileFilterUsed = false;

                            val returnVal: Int = showSaveDialog(null)
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                Configuration.savePath = this.selectedFile.absolutePath.replace("\\", "/") + "/"
                            }
                        }
                    }
                },
                shortcut = KeyShortcut(Key.V, ctrl = true)
            )
        }

    }

    showAlertDialog(
        showDialog = alertDialog
    )

}

fun loadPicture(url: String): ImageBitmap {
    return Image.makeFromEncoded(HttpClient().use { client ->
        runBlocking { client.get<ByteArray>(url) }
    }).asImageBitmap()
}


@Composable
fun CaptchaDialog(captchaUrl: String) {
    val textState = remember { mutableStateOf(TextFieldValue()) }

    Column {
        Image(
            bitmap = loadPicture(captchaUrl),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
        Row {
            TextField(
                label = { Text("Капча") },
                value = textState.value,
                onValueChange = { textState.value = it }
            )
            Button(
                modifier = Modifier
                    .weight(1F)
                    .height(55.dp)
                    .absolutePadding(left = 8.dp),
                onClick = {
                    FetchImageUrlsInteractor.currentCaptchaAnswerCallback.invoke(textState.value.text)
                }
            ) {
                Text("OK")
            }
        }
    }
}

@Composable
fun SearchWindow() {
    Column(Modifier.padding(16.dp)) {
        val textState = remember { mutableStateOf(TextFieldValue()) }
        val fetchUrlsProgress = remember { mutableStateOf(0f) }
        val currentStep = remember { mutableStateOf(0) }
        val trayState = rememberTrayState()
        val notification = rememberNotification("Image Language Detector", "Скачивание завершено!")

        Row {
            TextField(
                modifier = Modifier.width(280.dp),
                label = { Text("Запрос") },
                value = textState.value,
                singleLine = true,
                onValueChange = { textState.value = it }
            )
            Button(
                modifier = Modifier
                    .weight(1F)
                    .height(55.dp)
                    .absolutePadding(left = 8.dp),
                onClick = {
                    currentStep.value = 1
                    FetchImageUrlsInteractor.getImageUrls(textState.value.text,
                        onProgress = {
                            fetchUrlsProgress.value = it
                        },
                        onLoadAllPages = {
                            println("$it")
                            currentStep.value = 2
                            ImageDownloadInteractor.downloadAll(it, textState.value.text,
                                onProgress = {
                                    fetchUrlsProgress.value = it
                                    if (it == 1.0f){
                                        currentStep.value = 3
                                        ImageRecognizeInteractor.recognizeAll(textState.value.text, onProgress = {
                                            fetchUrlsProgress.value = it
                                            if (it == 1.0f){
                                                trayState.sendNotification(notification)
                                                currentStep.value = 4
                                            }
                                        })
                                    }
                                })
                        })
                }
            ) {
                Text("\uD83D\uDD0E")
            }
        }

        LinearProgressIndicator(
            progress = fetchUrlsProgress.value,
            modifier = Modifier.width(280.dp).alpha(if (fetchUrlsProgress.value != 0f) 1f else 0f)
        )

        Text(
            modifier = Modifier.absolutePadding(top = 16.dp).alpha(if (currentStep.value > 0) 1f else 0f),
            text = if (currentStep.value == 1) "Поиск картинок... " else "Поиск картинок - Готово. " ,
        )
        Text(
            modifier = Modifier.alpha(if (currentStep.value > 1) 1f else 0f),
            text = if (currentStep.value == 2) "Скачивание картинок... " else "Скачивание картинок - Готово. " ,
        )
        Text(
            modifier = Modifier.alpha(if (currentStep.value > 2) 1f else 0f),
            text = if (currentStep.value == 3) "Обработка картинок... " else "Обработка картинок - Готово. " ,
        )
        Row(modifier = Modifier.absolutePadding(top = 16.dp).alpha(if (currentStep.value > 3) 1f else 0f)) {
            Button(
                modifier = Modifier.weight(1F).height(40.dp),
                onClick = {
                    Desktop.getDesktop().open(File(textState.value.text))
                }
            ) { Text("Все изображения") }
            Button(
                modifier = Modifier.weight(1F).height(40.dp).absolutePadding(left = 8.dp),
                onClick = {
                    Desktop.getDesktop().open(File("ru_${textState.value.text}"))
                }
            ) { Text("Ru изображения") }
        }
    }
}

