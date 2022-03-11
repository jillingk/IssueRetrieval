import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.Instant

val token: String = System.getenv("GithubToken")

val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
}

enum class STATE{
    OPEN,
    CLOSED,
    ALL
}

enum class REPOSITORYTYPE {
    PUBLIC,
    PRIVATE,
    ALL
}

// Main block to retrieve issues from github
suspend fun githubIssues(org: String) {
    runBlocking {
        val repos = getRepositories(org)
        val issues = repos.map {
            async {
                addRepo(it.name , getIssues(org, it.name, STATE.OPEN))
            }
        }.awaitAll().filterNotNull();
        //counting nm of open issues
        openIssues(issues)
        writetoFile(issues.joinToString("\n\n"), "outputIssues")
        //writing to file

    }
}

// Counts number of open issues
suspend fun openIssues(issues: List<List<Issues>>){
    issues.forEach{
        var n = 0
        it.forEach{
            n++
        }
        if(it != emptyList<Issues>()) {
            println(it[0].repository + " has " + n + " open issues")
        }
    }
}

// Took me way to long to just add repo to the data class object (should be a shorter way)
suspend fun addRepo(repo: String, issues: List<Issues>): List<Issues> {
    issues.forEach{it.apply{repository = repo}}
    return issues
}

// getting issues w github API
suspend fun getIssues(org: String, repo: String, state: STATE): List<Issues> {
    return try {
        client.get("https://api.github.com/repos/$org/$repo/issues") {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                append(HttpHeaders.Authorization, "token $token")
            }
            parameter("state", state.toString().lowercase())
        }

    } catch (e: ClientRequestException) {
        println("No issues found in repo $repo for $org")
        emptyList<Issues>()
    }
}

// Get list of repo's for company git.
suspend fun getRepositories(org: String, type: REPOSITORYTYPE = REPOSITORYTYPE.PUBLIC): List<Repository> {
    return client.get("https://api.github.com/orgs/$org/repos?type=$type") {
        headers {
            append(HttpHeaders.Accept, "application/vnd.github.v3+json")
            append(HttpHeaders.Authorization, "token $token")
        }
    }
}

@Serializable
data class Repository(val id: String, val name: String, @SerialName("full_name") val fullName: String)

@Serializable
data class User(
    val login: String,
    val url: String,
    val type: String
)

@Serializable
data class Issues(
    var repository: String? = null,
    val html_url: String,
    val body: String?,
    val number: String,
    val title: String,
    val user: User,
    val state: String,
    val created_at: String
)