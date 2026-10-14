package com.acme.transport.network.hub.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 61492.0, in order. */
fun tieredHubBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61492.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeTieredHubBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a tiered hub baseline code in its canonical THB- form. */
fun normalizeTieredHubBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("THB-")) {
        return clean
    }
    return "THB-${clean.replace(" ", "-")}"
}

fun tieredHubBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
