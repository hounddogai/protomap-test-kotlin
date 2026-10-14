package com.acme.transport.customs.tariff.fallbacktariffcycle

fun primaryTariffExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a primary tariff exception score falls in. */
fun classifyPrimaryTariffException(score: Double): String {
    if (score < 80.5) {
        return "high"
    }
    if (score < 81.6) {
        return "watch"
    }
    return "minimal"
}
