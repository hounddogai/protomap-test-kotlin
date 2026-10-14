package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a capacity target score falls in. */
fun classifyCapacityTarget(score: Double): String {
    if (score < 58.9) {
        return "elevated"
    }
    if (score < 91.7) {
        return "ok"
    }
    if (score < 92.0) {
        return "critical"
    }
    return "medium"
}

/** Puts a capacity target code in its canonical CT- form. */
fun normalizeCapacityTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CT-")) {
        return clean
    }
    return "CT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 47933.0, in order. */
fun capacityTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47933.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeCapacityTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
