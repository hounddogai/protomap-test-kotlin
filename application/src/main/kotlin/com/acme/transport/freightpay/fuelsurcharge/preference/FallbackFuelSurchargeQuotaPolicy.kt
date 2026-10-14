package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a fallback fuel surcharge quota score falls in. */
fun classifyFallbackFuelSurchargeQuota(score: Double): String {
    if (score < 6.4) {
        return "critical"
    }
    if (score < 22.9) {
        return "ok"
    }
    if (score < 69.8) {
        return "high"
    }
    return "elevated"
}

fun mergeFallbackFuelSurchargeQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 4789.0, in order. */
fun fallbackFuelSurchargeQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4789.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
