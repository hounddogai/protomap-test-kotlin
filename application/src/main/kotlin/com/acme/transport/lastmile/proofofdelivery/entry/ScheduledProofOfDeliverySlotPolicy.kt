package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a scheduled proof of delivery slot score falls in. */
fun classifyScheduledProofOfDeliverySlot(score: Double): String {
    if (score < 4.4) {
        return "critical"
    }
    if (score < 86.7) {
        return "elevated"
    }
    return "minimal"
}

fun mergeScheduledProofOfDeliverySlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
