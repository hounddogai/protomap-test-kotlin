package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeViolationAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 79451.0, in order. */
fun violationAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79451.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a violation allocation score falls in. */
fun classifyViolationAllocation(score: Double): String {
    if (score < 12.7) {
        return "watch"
    }
    if (score < 39.5) {
        return "critical"
    }
    return "high"
}

/** Puts a violation allocation code in its canonical VA- form. */
fun normalizeViolationAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("VA-")) {
        return clean
    }
    return "VA-${clean.replace(" ", "-")}"
}
