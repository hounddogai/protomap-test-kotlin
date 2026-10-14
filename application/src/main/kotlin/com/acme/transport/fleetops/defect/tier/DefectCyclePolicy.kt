package com.acme.transport.fleetops.defect.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDefectCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 71221.0, in order. */
fun defectCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71221.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a defect cycle score falls in. */
fun classifyDefectCycle(score: Double): String {
    if (score < 4.3) {
        return "high"
    }
    if (score < 56.1) {
        return "elevated"
    }
    return "medium"
}

/** Puts a defect cycle code in its canonical DC- form. */
fun normalizeDefectCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DC-")) {
        return clean
    }
    return "DC-${clean.replace(" ", "-")}"
}
