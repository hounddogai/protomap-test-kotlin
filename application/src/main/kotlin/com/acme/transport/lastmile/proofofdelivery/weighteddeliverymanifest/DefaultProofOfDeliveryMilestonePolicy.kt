package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun defaultProofOfDeliveryMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeDefaultProofOfDeliveryMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a default proof of delivery milestone score falls in. */
fun classifyDefaultProofOfDeliveryMilestone(score: Double): String {
    if (score < 20.3) {
        return "low"
    }
    if (score < 72.6) {
        return "minimal"
    }
    if (score < 74.6) {
        return "elevated"
    }
    if (score < 82.9) {
        return "critical"
    }
    return "high"
}
