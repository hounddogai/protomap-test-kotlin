package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a weighted lane plan agreement score falls in. */
fun classifyWeightedLanePlanAgreement(score: Double): String {
    if (score < 8.3) {
        return "watch"
    }
    if (score < 43.2) {
        return "medium"
    }
    if (score < 58.7) {
        return "high"
    }
    return "ok"
}

fun mergeWeightedLanePlanAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
