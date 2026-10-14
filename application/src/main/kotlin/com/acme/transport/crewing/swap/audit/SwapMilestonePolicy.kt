package com.acme.transport.crewing.swap.audit

/** Puts a swap milestone code in its canonical SM- form. */
fun normalizeSwapMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SM-")) {
        return clean
    }
    return "SM-${clean.replace(" ", "-")}"
}

/** Names the band a swap milestone score falls in. */
fun classifySwapMilestone(score: Double): String {
    if (score < 15.8) {
        return "ok"
    }
    if (score < 63.0) {
        return "high"
    }
    return "minimal"
}
