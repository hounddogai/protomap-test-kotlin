package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a primary redelivery claim score falls in. */
fun classifyPrimaryRedeliveryClaim(score: Double): String {
    if (score < 11.6) {
        return "critical"
    }
    if (score < 11.9) {
        return "minimal"
    }
    if (score < 63.9) {
        return "high"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 15061.0, in order. */
fun primaryRedeliveryClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15061.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePrimaryRedeliveryClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
