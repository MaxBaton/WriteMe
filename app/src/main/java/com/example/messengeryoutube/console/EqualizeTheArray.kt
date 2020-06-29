package com.example.messengeryoutube.console

class EqualizeTheArray {
    fun equalizeArray(arr: Array<Int>): Int {
        val sortedArray = arr.sortedArray()
        var numEquals = 0
        var maxNumEquals = 0
        for (i in 1..arr.size) {
            if (i != arr.size) {
                if (sortedArray[i] == sortedArray[i - 1]) {
                    numEquals++
                } else {
                    if (maxNumEquals < numEquals) {
                        maxNumEquals = numEquals
                    }
                    numEquals = 0
                }
            }else {
                if (maxNumEquals < numEquals) {
                    maxNumEquals = numEquals
                }
            }
        }
        val k = arr.size - (maxNumEquals + 1)
        return k
    }
}