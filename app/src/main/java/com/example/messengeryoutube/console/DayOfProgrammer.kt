package com.example.messengeryoutube.console

import kotlin.math.abs

fun main() {
    println(dayOfProgrammer(2017))
}

fun dayOfProgrammer(year: Int): String {
    val additional1918Days = if (year == 1918) 13 else 0
    val additionalDayInFebruary = if (year<1917 && year%4 == 0) {
        1
    }else if (year > 1918 && (year%400 == 0 || (year%4 == 0 && year%100 != 0))) {
        1
    }else 0
    val day = if (additional1918Days == 0) {
        13 - additionalDayInFebruary
    } else {
        26 - additionalDayInFebruary
    }
    return  finalDate(day,9,year)
}

fun finalDate(restOfDays: Int, numOfMonth: Int, year: Int): String {
    val restOfDaysString = if (restOfDays < 10) "0${restOfDays}" else restOfDays.toString()
    val numOfMonthString = if (numOfMonth < 10) "0${numOfMonth}" else numOfMonth.toString()
    return "${restOfDaysString}.${numOfMonthString}.${year}"
}
