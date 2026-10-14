package com.acme.transport.safety.violation.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeAdaptiveViolationCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive violation cycle score falls in. */
fun classifyAdaptiveViolationCycle(score: Double): String {
    if (score < 25.7) {
        return "elevated"
    }
    if (score < 47.3) {
        return "ok"
    }
    return "medium"
}

/** Puts a adaptive violation cycle code in its canonical AVC- form. */
fun normalizeAdaptiveViolationCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AVC-")) {
        return clean
    }
    return "AVC-${clean.replace(" ", "-")}"
}
