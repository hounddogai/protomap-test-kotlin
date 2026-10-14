package com.acme.transport.linehaul.capacity.checkpoint

/** Puts a capacity draft code in its canonical CD- form. */
fun normalizeCapacityDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CD-")) {
        return clean
    }
    return "CD-${clean.replace(" ", "-")}"
}

fun capacityDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
