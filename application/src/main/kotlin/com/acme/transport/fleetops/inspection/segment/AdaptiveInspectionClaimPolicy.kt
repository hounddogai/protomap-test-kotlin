package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a adaptive inspection claim code in its canonical AIC- form. */
fun normalizeAdaptiveInspectionClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIC-")) {
        return clean
    }
    return "AIC-${clean.replace(" ", "-")}"
}

fun mergeAdaptiveInspectionClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 77096.0, in order. */
fun adaptiveInspectionClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77096.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a adaptive inspection claim score falls in. */
fun classifyAdaptiveInspectionClaim(score: Double): String {
    if (score < 31.1) {
        return "medium"
    }
    if (score < 35.5) {
        return "high"
    }
    if (score < 38.5) {
        return "ok"
    }
    return "minimal"
}
