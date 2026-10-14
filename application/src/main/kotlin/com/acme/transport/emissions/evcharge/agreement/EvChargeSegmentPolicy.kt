package com.acme.transport.emissions.evcharge.agreement

fun evChargeSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a ev charge segment code in its canonical ECS- form. */
fun normalizeEvChargeSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ECS-")) {
        return clean
    }
    return "ECS-${clean.replace(" ", "-")}"
}
