package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun brokerEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a broker entry code in its canonical BE- form. */
fun normalizeBrokerEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BE-")) {
        return clean
    }
    return "BE-${clean.replace(" ", "-")}"
}

/** Names the band a broker entry score falls in. */
fun classifyBrokerEntry(score: Double): String {
    if (score < 38.9) {
        return "critical"
    }
    if (score < 71.2) {
        return "minimal"
    }
    return "elevated"
}

fun mergeBrokerEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
