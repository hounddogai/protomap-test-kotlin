package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRegionalCarbonReportAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 81498.0, in order. */
fun regionalCarbonReportAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81498.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional carbon report assignment code in its canonical RCRA- form. */
fun normalizeRegionalCarbonReportAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCRA-")) {
        return clean
    }
    return "RCRA-${clean.replace(" ", "-")}"
}
