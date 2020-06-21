package com.example.messengeryoutube.console

fun main() {
    println(jumpingOnClouds(arrayOf(1,1,1,0,1,1,0,0,0,0),3))
}

fun jumpingOnClouds(c: Array<Int>, k: Int): Int {
    var e = 100
    var j = 0
    var start = 0
    var round = 0
    do {
        if (round > 0) {
            start = (start + k) % c.size
        }
        for (i in start until c.size step k) {
            e -= if (c[i] == 1) 3 else 1
            j = c.size - i
            start = i
        }
        round++
    }while (j != k)
    return e
}