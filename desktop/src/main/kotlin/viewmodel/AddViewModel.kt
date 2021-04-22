package viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.http.*
import kotlinx.coroutines.launch
import model.FileDetails
import model.addDataset
import model.defaultColor
import model.defaultName
import view.Message
import view.chooseFile

class AddViewModel(private val mainViewModel: MainViewModel)
{
    val name: MutableState<String> = mutableStateOf(defaultName)
    val selectedColor: MutableState<Long> = mutableStateOf(defaultColor)
    val file: MutableState<FileDetails?> = mutableStateOf(null)
    val adding = mutableStateOf(false)

    fun fileSelected()
    {
        mainViewModel.coroutineScope.launch {
            val fileObject = chooseFile() ?: return@launch
            file.value = runCatching {
                csvReader().openAsync(fileObject) {
                    val header = readNext() ?: return@openAsync null
                    val itemNumber = readAllAsSequence().count()
                    return@openAsync FileDetails(fileObject, itemNumber, header)
                }
            }.getOrNull() ?: return@launch
        }
    }

    fun add()
    {
        if (adding.value) return
        adding.value = true

        var message by mainViewModel.message
        mainViewModel.coroutineScope.launch {
            val currentFile = file.value
            when
            {
                currentFile == null ->
                {
                    message =
                        Message(
                            "You need to choose a file for the dataset",
                            false
                        )

                }
                name.value == "" ->
                {
                    message =
                        Message(
                            "You need to choose a name for the dataset",
                            false
                        )

                }
                else ->
                {
                    val currentConfiguration = mainViewModel.configuration.value ?: return@launch
                    when (addDataset(
                        mainViewModel.client,
                        currentConfiguration,
                        name.value,
                        selectedColor.value.toString(16),
                        currentFile.fileObject
                    ))
                    {
                        HttpStatusCode.OK -> message =
                            Message(
                                "Dataset added",
                                true
                            )

                        HttpStatusCode.Unauthorized ->
                        {
                            message = Message("Credentials  invalid!", false)
                            mainViewModel.configuration.value = null
                        }
                        HttpStatusCode.Conflict -> message =
                            Message(
                                "Dataset already exists",
                                false
                            )

                        HttpStatusCode.BadRequest -> message =
                            Message(
                                "Invalid dataset",
                                false
                            )

                        null -> message =
                            Message(
                                "Connection to server failed",
                                false
                            )

                        else -> message = Message("Unknown error occured", false)
                    }
                }
            }
            adding.value = false
            name.value = defaultName
            selectedColor.value = defaultColor
            file.value = null
        }

    }
}
