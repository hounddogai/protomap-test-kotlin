package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeBatchedUtilizationBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a batched utilization budget code in its canonical BUB- form. */
fun normalizeBatchedUtilizationBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BUB-")) {
        return clean
    }
    return "BUB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 95047.0, in order. */
fun batchedUtilizationBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 95047.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedUtilizationBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
