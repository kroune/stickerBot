import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.util.SendingFile
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

const val TOKEN = "7057845429:AAFDrd5EEpkCetOQ6F94oI55vI7h3-3outQ"
val bot: Bot = Bot.createPolling(TOKEN)

fun main() {
    bot.start()
    bot.onMessage {
        if (it.photo.isNotEmpty()) {
            it.photo.groupBy { it2 -> it2.fileId.dropLast(7) }.forEach { it1 ->
                val photo = it1.value.last()
                val b = bot.getFile(photo.fileId)
                println(b.fileId)
                println(b.filePath)
                println(b.fileUniqueId)
                println()
                val url = URL("https://api.telegram.org/file/bot$TOKEN/${b.filePath}")
                url.openStream().use { Files.copy(it, Paths.get("input.jpg")) }
                val inputFile = File("input.jpg")
                val outputFile = File("output.png")
                if (photo.height > photo.width) {
                    resizeImageUsingJavaIO(
                        inputFile,
                        outputFile,
                        round(photo.width.toFloat() / photo.height * 512).toInt(),
                        512
                    )
                } else {
                    resizeImageUsingJavaIO(
                        inputFile,
                        outputFile,
                        512,
                        round(photo.height.toFloat() / photo.width * 512).toInt()
                    )
                }
                bot.sendDocument(it.chat.id.toChatId(), SendingFile(outputFile))
                inputFile.delete()
                outputFile.delete()
            }
        } else {
            bot.sendMessage(it.chat.id.toChatId(), "no")
        }
    }
}

fun resizeImageUsingJavaIO(inputFile: File, outputFile: File, newWidth: Int, newHeight: Int) {
    val inputImage = ImageIO.read(inputFile)
    val outputImage = BufferedImage(newWidth, newHeight, inputImage.type)
    val graphics2D: Graphics2D = outputImage.createGraphics()
    graphics2D.drawImage(
        inputImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),
        0,
        0,
        newWidth,
        newHeight,
        null
    )
    graphics2D.dispose()
    ImageIO.write(outputImage, "png", outputFile)
}
