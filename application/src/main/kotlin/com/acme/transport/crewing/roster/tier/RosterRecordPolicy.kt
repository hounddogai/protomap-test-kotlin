package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a roster record score falls in. */
fun classifyRosterRecord(score: Double): String {
    if (score < 17.1) {
        return "high"
    }
    if (score < 32.9) {
        return "critical"
    }
    if (score < 44.0) {
        return "ok"
    }
    if (score < 82.5) {
        return "minimal"
    }
    return "elevated"
}

/** Puts a roster record code in its canonical RR- form. */
fun normalizeRosterRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RR-")) {
        return clean
    }
    return "RR-${clean.replace(" ", "-")}"
}

fun mergeRosterRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rosterRecordBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
