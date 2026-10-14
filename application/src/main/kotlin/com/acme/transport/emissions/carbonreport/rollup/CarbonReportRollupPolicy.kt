package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a carbon report rollup code in its canonical CRR- form. */
fun normalizeCarbonReportRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRR-")) {
        return clean
    }
    return "CRR-${clean.replace(" ", "-")}"
}

fun mergeCarbonReportRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 8362.0, in order. */
fun carbonReportRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8362.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun carbonReportRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
