package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 56452.0, in order. */
fun rollingTenderRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56452.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRollingTenderRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a rolling tender rollup code in its canonical RTR- form. */
fun normalizeRollingTenderRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTR-")) {
        return clean
    }
    return "RTR-${clean.replace(" ", "-")}"
}

fun rollingTenderRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
