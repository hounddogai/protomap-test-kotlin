package com.acme.transport.customs.declaration.budget

/** Names the band a rolling declaration tier score falls in. */
fun classifyRollingDeclarationTier(score: Double): String {
    if (score < 40.2) {
        return "medium"
    }
    if (score < 79.2) {
        return "elevated"
    }
    return "low"
}

fun rollingDeclarationTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
