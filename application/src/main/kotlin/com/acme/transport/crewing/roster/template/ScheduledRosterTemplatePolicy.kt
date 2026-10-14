package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.numbers.roundTo

fun scheduledRosterTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a scheduled roster template code in its canonical SRT- form. */
fun normalizeScheduledRosterTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRT-")) {
        return clean
    }
    return "SRT-${clean.replace(" ", "-")}"
}

fun scheduledRosterTemplateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
