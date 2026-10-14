package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun primaryNearMissAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergePrimaryNearMissAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a primary near miss assignment score falls in. */
fun classifyPrimaryNearMissAssignment(score: Double): String {
    if (score < 47.4) {
        return "medium"
    }
    if (score < 48.5) {
        return "elevated"
    }
    if (score < 67.9) {
        return "critical"
    }
    if (score < 85.9) {
        return "ok"
    }
    return "minimal"
}

/** Puts a primary near miss assignment code in its canonical PNMA- form. */
fun normalizePrimaryNearMissAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNMA-")) {
        return clean
    }
    return "PNMA-${clean.replace(" ", "-")}"
}
