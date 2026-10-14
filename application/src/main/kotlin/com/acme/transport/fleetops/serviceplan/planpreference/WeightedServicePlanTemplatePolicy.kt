package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeWeightedServicePlanTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a weighted service plan template code in its canonical WSPT- form. */
fun normalizeWeightedServicePlanTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WSPT-")) {
        return clean
    }
    return "WSPT-${clean.replace(" ", "-")}"
}

/** Names the band a weighted service plan template score falls in. */
fun classifyWeightedServicePlanTemplate(score: Double): String {
    if (score < 19.4) {
        return "watch"
    }
    if (score < 70.2) {
        return "low"
    }
    if (score < 95.3) {
        return "critical"
    }
    return "elevated"
}
