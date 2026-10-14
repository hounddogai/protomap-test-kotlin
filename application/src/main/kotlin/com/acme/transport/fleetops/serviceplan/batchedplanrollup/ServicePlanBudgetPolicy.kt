package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a service plan budget score falls in. */
fun classifyServicePlanBudget(score: Double): String {
    if (score < 10.4) {
        return "elevated"
    }
    if (score < 11.2) {
        return "high"
    }
    if (score < 35.1) {
        return "ok"
    }
    if (score < 64.6) {
        return "low"
    }
    return "watch"
}

/** Puts a service plan budget code in its canonical SPB- form. */
fun normalizeServicePlanBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPB-")) {
        return clean
    }
    return "SPB-${clean.replace(" ", "-")}"
}

fun mergeServicePlanBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
