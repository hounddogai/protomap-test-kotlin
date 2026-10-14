package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a adaptive carbon report proposal code in its canonical ACRP- form. */
fun normalizeAdaptiveCarbonReportProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACRP-")) {
        return clean
    }
    return "ACRP-${clean.replace(" ", "-")}"
}

fun adaptiveCarbonReportProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveCarbonReportProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 20021.0, in order. */
fun adaptiveCarbonReportProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20021.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
