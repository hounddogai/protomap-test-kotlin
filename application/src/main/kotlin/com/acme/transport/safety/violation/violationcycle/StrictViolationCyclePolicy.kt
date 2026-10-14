package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a strict violation cycle score falls in. */
fun classifyStrictViolationCycle(score: Double): String {
    if (score < 28.6) {
        return "watch"
    }
    if (score < 68.9) {
        return "critical"
    }
    if (score < 99.2) {
        return "minimal"
    }
    return "medium"
}

/** Puts a strict violation cycle code in its canonical SVC- form. */
fun normalizeStrictViolationCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SVC-")) {
        return clean
    }
    return "SVC-${clean.replace(" ", "-")}"
}

fun mergeStrictViolationCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
