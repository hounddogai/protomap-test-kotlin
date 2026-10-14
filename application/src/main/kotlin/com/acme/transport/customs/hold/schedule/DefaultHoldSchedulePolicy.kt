package com.acme.transport.customs.hold.schedule

/** Puts a default hold schedule code in its canonical DHS- form. */
fun normalizeDefaultHoldScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DHS-")) {
        return clean
    }
    return "DHS-${clean.replace(" ", "-")}"
}

fun defaultHoldScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
