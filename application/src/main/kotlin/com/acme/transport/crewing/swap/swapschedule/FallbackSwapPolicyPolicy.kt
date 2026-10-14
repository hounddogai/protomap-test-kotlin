package com.acme.transport.crewing.swap.swapschedule

/** Puts a fallback swap policy code in its canonical FSP- form. */
fun normalizeFallbackSwapPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSP-")) {
        return clean
    }
    return "FSP-${clean.replace(" ", "-")}"
}

/** Names the band a fallback swap policy score falls in. */
fun classifyFallbackSwapPolicy(score: Double): String {
    if (score < 2.2) {
        return "high"
    }
    if (score < 2.6) {
        return "low"
    }
    if (score < 81.7) {
        return "watch"
    }
    return "minimal"
}

fun fallbackSwapPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
