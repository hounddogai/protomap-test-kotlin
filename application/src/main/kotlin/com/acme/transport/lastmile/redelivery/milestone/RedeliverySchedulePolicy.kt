package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun redeliveryScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeRedeliveryScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a redelivery schedule score falls in. */
fun classifyRedeliverySchedule(score: Double): String {
    if (score < 16.4) {
        return "ok"
    }
    if (score < 73.5) {
        return "watch"
    }
    if (score < 90.5) {
        return "elevated"
    }
    return "high"
}

/** Puts a redelivery schedule code in its canonical RS- form. */
fun normalizeRedeliveryScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RS-")) {
        return clean
    }
    return "RS-${clean.replace(" ", "-")}"
}
