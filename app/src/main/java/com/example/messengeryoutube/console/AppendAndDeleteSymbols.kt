package com.example.messengeryoutube.console

class AppendAndDeleteSymbols {
    fun appendAndDelete(s: String, t: String, k: Int): String {
        var necessaryK = 0
        var j = 0
        val maxStr = if (s.length > t.length) s else t
        for (i in 0 until maxStr.length) {
            if (s.length > i && t.length > i) {
                if (s[i] != t[i]) {
                    j = i
                    break
                }
            }else {
                j = i
                break
            }
        }
        necessaryK = (s.length - j) + (t.length - j)
//        if(necessaryK < k ) {
//            if (maxStr == t) {
//                for (i in 0 until (necessaryK - k)) {
//                    necessaryK++
//                    if (necessaryK == k) break
//                }
//            }
//        }
        return if (necessaryK == k) "Yes" else "No"
    }
}