package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a swap target score falls in. */
fun classifySwapTarget(score: Double): String {
    if (score < 19.6) {
        return "minimal"
    }
    if (score < 36.1) {
        return "low"
    }
    if (score < 40.6) {
        return "elevated"
    }
    return "critical"
}

/** Puts a swap target code in its canonical ST- form. */
fun normalizeSwapTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ST-")) {
        return clean
    }
    return "ST-${clean.replace(" ", "-")}"
}

fun swapTargetBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
