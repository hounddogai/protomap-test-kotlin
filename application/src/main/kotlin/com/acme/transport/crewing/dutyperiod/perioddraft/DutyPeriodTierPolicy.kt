package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a duty period tier code in its canonical DPT- form. */
fun normalizeDutyPeriodTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPT-")) {
        return clean
    }
    return "DPT-${clean.replace(" ", "-")}"
}

fun mergeDutyPeriodTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a duty period tier score falls in. */
fun classifyDutyPeriodTier(score: Double): String {
    if (score < 5.3) {
        return "minimal"
    }
    if (score < 17.1) {
        return "medium"
    }
    if (score < 55.7) {
        return "ok"
    }
    if (score < 74.9) {
        return "elevated"
    }
    return "high"
}

fun dutyPeriodTierBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
