package com.acme.transport.crewing.swap.recorditem

fun manualSwapScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a manual swap score code in its canonical MSS- form. */
fun normalizeManualSwapScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MSS-")) {
        return clean
    }
    return "MSS-${clean.replace(" ", "-")}"
}
