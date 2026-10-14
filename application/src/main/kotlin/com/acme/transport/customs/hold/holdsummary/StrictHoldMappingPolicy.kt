package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.numbers.roundTo

/** Puts a strict hold mapping code in its canonical SHM- form. */
fun normalizeStrictHoldMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHM-")) {
        return clean
    }
    return "SHM-${clean.replace(" ", "-")}"
}

fun strictHoldMappingBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a strict hold mapping score falls in. */
fun classifyStrictHoldMapping(score: Double): String {
    if (score < 1.9) {
        return "critical"
    }
    if (score < 2.2) {
        return "high"
    }
    if (score < 52.2) {
        return "elevated"
    }
    if (score < 81.4) {
        return "medium"
    }
    return "watch"
}
