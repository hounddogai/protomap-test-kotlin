package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeServiceAreaTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a service area template score falls in. */
fun classifyServiceAreaTemplate(score: Double): String {
    if (score < 37.1) {
        return "minimal"
    }
    if (score < 71.2) {
        return "critical"
    }
    if (score < 73.4) {
        return "ok"
    }
    if (score < 78.1) {
        return "low"
    }
    return "elevated"
}
