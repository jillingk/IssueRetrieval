import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import netscape.javascript.JSObject
import java.io.File


suspend fun main() {
    //get open issues from adyen-examples
    githubIssues("adyen-examples")


    client.close()
}

// Changed the write to file function so it just writes strings so I don't have to deal with different objects in method. To print anything convert it to a string with -  .joinToString((\n\n) - )
suspend fun writetoFile(printText: String, name: String) {
    val fileName = name + ".txt"
    // Create filen with filename
    var file = File(fileName)
    // create a new file
    val isNewFileCreated :Boolean = file.createNewFile()
    if(isNewFileCreated){
        println("$fileName is created successfully with JSON strings")
    } else {
        println("$fileName is updated")
        file.delete()
        file.createNewFile()

    }
    file.writeText(printText)
}


// Simply printing in either JSON or TXT.
enum class FORMAT {
    JSON,
    TXT
}








