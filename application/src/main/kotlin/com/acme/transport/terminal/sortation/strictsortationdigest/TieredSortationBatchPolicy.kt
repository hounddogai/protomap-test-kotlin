package com.acme.transport.terminal.sortation.strictsortationdigest

fun tieredSortationBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a tiered sortation batch code in its canonical TSB- form. */
fun normalizeTieredSortationBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TSB-")) {
        return clean
    }
    return "TSB-${clean.replace(" ", "-")}"
}
