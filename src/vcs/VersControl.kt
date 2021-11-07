package vcs

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

fun main(args: Array<String>) {
    val (config, index) = initProject()

    val commits = File("vcs\\commits")
    commits.mkdir()

    val fileHash = getHashMapFiles(index)

    var command = getCommand(args)

    println(command)

    val regexConfig = "^config$".toRegex()
    val regexConfigWithArgument = "config\\s(\\w+)".toRegex()
    val regexAdd = "^add$".toRegex()
    val regexAddFile = "add\\s(\\w+)?\\.?(\\w+)?".toRegex()
    val regexAddCommit = "commit\\s.*".toRegex()
    val regexEmptyCommit = "^commit$".toRegex()

    when {
        regexConfig.matches(command) -> {
            if (config.readLines().isEmpty()) {
                println("Please, tell me who you are.")
            } else if (config.readLines().isNotEmpty()) {
                println("The username is ${config.readText()}.")
            }
        }
        regexConfigWithArgument.matches(command) -> {
            config.writeText(command.split(" ")[1])
            println("The username is ${config.readText()}.")
        }
        regexAdd.matches(command) -> {
            if (index.readLines().isEmpty()) {
                println("Add a file to the index.")
            } else {
                println("Tracked files:")
                index.forEachLine { println(it) }
            }
        }
        regexAddFile.matches(command) -> {
            val nameFile = command.split(" ")[1]
            val file = File(File(".").canonicalPath + "\\" + nameFile)

            val allNameFiles = index.readLines().toMutableList()

            if (file.exists() && nameFile !in allNameFiles) {
                index.appendText("$nameFile\n")
                println("The file '$nameFile' is tracked.")
            } else {
                println("Can't find '$nameFile'.")
            }
        }

        regexAddCommit.matches(command) -> {
            if (index.readLines().isNotEmpty() && checkChangeInFiles(index, fileHash)) {
                val file = File("vcs\\commits\\${stringToSHA(index.readLines()[0])}")
                file.mkdir()
                for (readLine in index.readLines()) {
                    File(readLine)
                        .let { sourceFile ->
                            sourceFile.copyTo(File("${file.path}\\$readLine"))
                        }
                }
            }else{
                println("nothing to commit, working tree clean")
            }
            index.deleteRecursively()
        }

        regexEmptyCommit.matches(command) -> println("Message was not passed")

        "--help" in args || args.isEmpty() -> showHelp()
        "log" in args -> println("Show commit logs.")
        //"commit" in args -> println("Save changes.")
        "checkout" in args -> println("Restore a file.")
        else -> println("\'${args[0]}\' is not a SVCS command.")
    }
}

private fun getHashMapFiles(index: File): HashMap<String, String> {
    val fileHash = HashMap<String, String>()

    val readIndexFile = index.readLines().toMutableList()

    for (line in readIndexFile) {
        File(line).let {
            fileHash.put(line, File(line).sha1())
        }
    }
    return fileHash
}

private fun initProject(): Pair<File, File> {
    val vcs = File("vcs")
    vcs.mkdir()
    val config = vcs.resolve("config.txt")
    config.createNewFile()
    val index = vcs.resolve("index.txt")
    index.createNewFile()
    return Pair(config, index)
}

private fun getCommand(args: Array<String>): String {
    var command = ""
    for (arg in args) {
        command += " $arg"
    }
    command = command.trim()
    return command
}

private fun showHelp() {
    println(
        "These are SVCS commands:\n" +
                "config     Get and set a username.\n" +
                "add        Add a file to the index.\n" +
                "log        Show commit logs.\n" +
                "commit     Save changes.\n" +
                "checkout   Restore a file."
    )
}


private fun File.sha1(): String {
    val md = MessageDigest.getInstance("SHA1")
    return this.inputStream().use { fis ->
        val buffer = ByteArray(8192)
        generateSequence {
            when (val bytesRead = fis.read(buffer)) {
                -1 -> null
                else -> bytesRead
            }
        }.forEach { bytesRead -> md.update(buffer, 0, bytesRead) }
        md.digest().joinToString("") { "%02x".format(it) }
    }
}

fun checkChangeInFiles(file: File, fileHash: HashMap<String, String>): Boolean {
    val hashMapFiles = getHashMapFiles(file)
    if (hashMapFiles == fileHash)
        return true
    return false
}

fun stringToSHA(input: String): String {
    val md = MessageDigest.getInstance("SHA1")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

