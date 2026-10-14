package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a capped dispute agreement score falls in. */
fun classifyCappedDisputeAgreement(score: Double): String {
    if (score < 25.4) {
        return "ok"
    }
    if (score < 57.3) {
        return "medium"
    }
    return "watch"
}

fun mergeCappedDisputeAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
