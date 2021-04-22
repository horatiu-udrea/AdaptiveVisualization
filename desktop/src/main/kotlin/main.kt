import androidx.compose.desktop.Window
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import view.MainWindow
import viewmodel.MainViewModel
import java.awt.image.BufferedImage
import java.io.File
import java.security.Security
import javax.imageio.ImageIO

fun main()
{
    System.setProperty("io.ktor.random.secure.random.provider", "DRBG")
    Security.setProperty("securerandom.drbg.config", "HMAC_DRBG,SHA-512,256,pr_and_reseed")

    val coroutineScope = CoroutineScope(Dispatchers.Default)
    val viewModel = MainViewModel(coroutineScope)
    Window(title = "Dataset Configuration", size = IntSize(1000, 800), icon = getWindowIcon()) {
        MainWindow(viewModel)
    }
}

fun getResource(path: String): File?
{
    return Thread.currentThread().contextClassLoader.getResource(path)?.toURI()?.let { File(it) }
}

fun getWindowIcon(): BufferedImage
{
    var image: BufferedImage? = null
    try
    {
        image = ImageIO.read(getResource("images/icon.png"))
    }
    catch (e: Exception)
    {
        // image file does not exist
        e.printStackTrace()
    }

    if (image == null)
    {
        image = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    }

    return image
}
