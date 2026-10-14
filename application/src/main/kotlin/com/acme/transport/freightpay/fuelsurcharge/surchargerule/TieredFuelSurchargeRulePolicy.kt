package com.acme.transport.freightpay.fuelsurcharge.surchargerule

fun tieredFuelSurchargeRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a tiered fuel surcharge rule score falls in. */
fun classifyTieredFuelSurchargeRule(score: Double): String {
    if (score < 38.4) {
        return "medium"
    }
    if (score < 39.1) {
        return "low"
    }
    if (score < 55.2) {
        return "critical"
    }
    if (score < 75.3) {
        return "high"
    }
    return "ok"
}
