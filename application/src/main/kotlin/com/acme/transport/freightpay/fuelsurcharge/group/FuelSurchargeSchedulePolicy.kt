package com.acme.transport.freightpay.fuelsurcharge.group

/** Names the band a fuel surcharge schedule score falls in. */
fun classifyFuelSurchargeSchedule(score: Double): String {
    if (score < 25.6) {
        return "medium"
    }
    if (score < 47.2) {
        return "watch"
    }
    if (score < 84.2) {
        return "critical"
    }
    return "elevated"
}

fun fuelSurchargeScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
