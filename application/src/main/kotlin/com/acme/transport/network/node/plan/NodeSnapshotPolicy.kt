package com.acme.transport.network.node.plan

import com.acme.transport.kit.numbers.roundTo

fun nodeSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a node snapshot code in its canonical NS- form. */
fun normalizeNodeSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NS-")) {
        return clean
    }
    return "NS-${clean.replace(" ", "-")}"
}

fun nodeSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
