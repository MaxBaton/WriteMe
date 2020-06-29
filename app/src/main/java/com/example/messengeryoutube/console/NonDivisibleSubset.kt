package com.example.messengeryoutube.console

class NonDivisibleSubset {
    fun nonDivisibleSubset(k: Int, s: Array<Int>): Int {
        var mutableListS = s.toMutableList()
        var repeatedNumbers = mutableListOf<Int>()
        val deletedNumbers = mutableSetOf<Int>()
        for (i in 0 until s.size) {
            for (j in i+1 until s.size) {
                if ((s[i] + s[j]) % k == 0) {
                    repeatedNumbers.add(s[i])
                    repeatedNumbers.add(s[j])
                }
            }
        }
        repeatedNumbers = repeatedNumbers.sorted().toMutableList()
        for (i in 1 until repeatedNumbers.size) {
            if (repeatedNumbers[i] == repeatedNumbers[i - 1]) deletedNumbers.add(repeatedNumbers[i])
        }
        mutableListS = mutableListS.filter { !deletedNumbers.contains(it) }.toMutableList()
        return mutableListS.size
    }
}