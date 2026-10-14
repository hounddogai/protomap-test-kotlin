package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.kit.numbers.roundTo

fun fuelSurchargeSlotBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a fuel surcharge slot score falls in. */
fun classifyFuelSurchargeSlot(score: Double): String {
    if (score < 33.2) {
        return "watch"
    }
    if (score < 83.7) {
        return "ok"
    }
    if (score < 87.3) {
        return "high"
    }
    return "minimal"
}
