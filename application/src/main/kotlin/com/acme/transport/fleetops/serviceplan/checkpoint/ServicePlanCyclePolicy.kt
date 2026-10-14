package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a service plan cycle code in its canonical SPC- form. */
fun normalizeServicePlanCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPC-")) {
        return clean
    }
    return "SPC-${clean.replace(" ", "-")}"
}

/** Names the band a service plan cycle score falls in. */
fun classifyServicePlanCycle(score: Double): String {
    if (score < 40.6) {
        return "watch"
    }
    if (score < 45.6) {
        return "medium"
    }
    if (score < 86.9) {
        return "minimal"
    }
    return "high"
}

fun mergeServicePlanCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
