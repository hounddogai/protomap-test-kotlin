package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun fuelSurchargeExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a fuel surcharge exception score falls in. */
fun classifyFuelSurchargeException(score: Double): String {
    if (score < 1.8) {
        return "low"
    }
    if (score < 9.9) {
        return "elevated"
    }
    if (score < 22.1) {
        return "critical"
    }
    return "ok"
}

fun mergeFuelSurchargeExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
