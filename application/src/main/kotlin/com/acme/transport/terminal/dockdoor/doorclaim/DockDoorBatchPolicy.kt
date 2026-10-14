package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.numbers.roundTo

fun dockDoorBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun dockDoorBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a dock door batch code in its canonical DDB- form. */
fun normalizeDockDoorBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDB-")) {
        return clean
    }
    return "DDB-${clean.replace(" ", "-")}"
}
