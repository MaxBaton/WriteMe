package com.example.messengeryoutube.console

class LibraryFine {
    fun libraryFine(d1: Int, m1: Int, y1: Int, d2: Int, m2: Int, y2: Int): Int {
        val diffYears = y1 - y2
        val diffMonths = m1 - m2
        val diffDays = d1 - d2
        return when {
            isEarlier(diffYears,diffMonths,diffDays) -> 0
            diffYears  > 0 -> 10_000 * diffYears
            diffMonths > 0 -> 500 * diffMonths
            diffDays   > 0 -> 15 * diffDays
            else -> return 0
        }
    }

    private fun isEarlier(diffYears: Int, diffMonths: Int, diffDays: Int): Boolean {
        if (diffYears < 0) {
            return true
        }else if (diffYears == 0 && diffMonths < 0) {
            return true
        }else if (diffMonths == 0 && diffDays < 0) {
            return true
        }
        return false
    }
}