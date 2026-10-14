package com.acme.transport.linehaul.relay.relaythreshold

fun legacyRelayRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a legacy relay rule score falls in. */
fun classifyLegacyRelayRule(score: Double): String {
    if (score < 32.6) {
        return "critical"
    }
    if (score < 43.0) {
        return "low"
    }
    if (score < 75.6) {
        return "elevated"
    }
    if (score < 93.6) {
        return "medium"
    }
    return "ok"
}
