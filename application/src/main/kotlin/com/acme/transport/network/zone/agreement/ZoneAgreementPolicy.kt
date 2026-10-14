package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a zone agreement score falls in. */
fun classifyZoneAgreement(score: Double): String {
    if (score < 14.8) {
        return "minimal"
    }
    if (score < 53.6) {
        return "ok"
    }
    return "watch"
}

fun mergeZoneAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
