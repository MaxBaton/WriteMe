package com.example.messengeryoutube.console

import kotlinx.coroutines.*

fun main() {
    var pos = 0
    val job = CoroutineScope(Dispatchers.IO).launch { pos = printPosition() }
    CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.IO) {
            job.join()
        }
    }
    println("position = $pos")
}

private suspend fun printPosition(): Int {
    var posiiton = 0
    val job = CoroutineScope(Dispatchers.IO).launch {
        posiiton = getPosition()
    }
    job.join()
    return posiiton
}

private fun getPosition(): Int {
    Thread.sleep(2000)
    return 123
}

private suspend fun allPrint() {
    println("message 1")
    val job = CoroutineScope(Dispatchers.IO).launch { printMessageFromCoroutine("message 2") }
    job.join()
    println("message 3")
}

private fun printMessageFromCoroutine(message: String) {
    Thread.sleep(2000)
    println(message)
}