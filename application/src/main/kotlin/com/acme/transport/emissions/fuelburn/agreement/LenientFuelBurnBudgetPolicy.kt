package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a lenient fuel burn budget score falls in. */
fun classifyLenientFuelBurnBudget(score: Double): String {
    if (score < 28.6) {
        return "high"
    }
    if (score < 88.1) {
        return "medium"
    }
    if (score < 91.7) {
        return "elevated"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 51167.0, in order. */
fun lenientFuelBurnBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51167.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeLenientFuelBurnBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
