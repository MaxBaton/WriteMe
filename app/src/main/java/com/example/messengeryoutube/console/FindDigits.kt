package com.example.messengeryoutube.console

fun main() {
    println(findDigits(1012))
}

fun findDigits(n: Int): Int {
    val nStr = n.toString()
    val intList = mutableListOf<Int>()
    var numberOfDigits = 0
    nStr.forEach { intList.add(Integer.parseInt(it.toString())) }
    intList.forEach {
        if (it != 0) {
            if (n % it == 0) numberOfDigits++
        }
    }
    return numberOfDigits
}