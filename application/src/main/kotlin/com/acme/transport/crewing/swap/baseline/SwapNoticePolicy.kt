package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.numbers.roundTo

fun swapNoticeBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun swapNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a swap notice code in its canonical SN- form. */
fun normalizeSwapNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SN-")) {
        return clean
    }
    return "SN-${clean.replace(" ", "-")}"
}
