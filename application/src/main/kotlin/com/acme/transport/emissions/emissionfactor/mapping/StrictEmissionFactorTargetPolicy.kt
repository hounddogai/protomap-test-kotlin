package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a strict emission factor target score falls in. */
fun classifyStrictEmissionFactorTarget(score: Double): String {
    if (score < 35.8) {
        return "watch"
    }
    if (score < 82.5) {
        return "elevated"
    }
    return "medium"
}

fun mergeStrictEmissionFactorTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
