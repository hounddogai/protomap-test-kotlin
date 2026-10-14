package com.acme.transport.linehaul.backhaul.audit

/** Puts a backhaul target code in its canonical BT- form. */
fun normalizeBackhaulTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BT-")) {
        return clean
    }
    return "BT-${clean.replace(" ", "-")}"
}

fun backhaulTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
