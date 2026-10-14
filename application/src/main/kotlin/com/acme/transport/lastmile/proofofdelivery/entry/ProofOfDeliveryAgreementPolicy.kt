package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a proof of delivery agreement score falls in. */
fun classifyProofOfDeliveryAgreement(score: Double): String {
    if (score < 33.4) {
        return "elevated"
    }
    if (score < 48.6) {
        return "minimal"
    }
    if (score < 67.1) {
        return "high"
    }
    if (score < 95.2) {
        return "low"
    }
    return "critical"
}

fun mergeProofOfDeliveryAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
