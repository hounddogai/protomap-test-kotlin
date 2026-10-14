package com.acme.transport.crewing.swap.recorditem

fun tieredSwapCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a tiered swap checkpoint code in its canonical TSC- form. */
fun normalizeTieredSwapCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TSC-")) {
        return clean
    }
    return "TSC-${clean.replace(" ", "-")}"
}
