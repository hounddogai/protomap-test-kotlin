package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.numbers.roundTo

/** Puts a tender schedule code in its canonical TS- form. */
fun normalizeTenderScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TS-")) {
        return clean
    }
    return "TS-${clean.replace(" ", "-")}"
}

fun tenderScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun tenderScheduleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
