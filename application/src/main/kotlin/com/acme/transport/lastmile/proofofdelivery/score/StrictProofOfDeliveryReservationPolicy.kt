package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a strict proof of delivery reservation score falls in. */
fun classifyStrictProofOfDeliveryReservation(score: Double): String {
    if (score < 9.9) {
        return "watch"
    }
    if (score < 32.0) {
        return "low"
    }
    if (score < 33.0) {
        return "medium"
    }
    if (score < 70.8) {
        return "ok"
    }
    return "critical"
}

fun mergeStrictProofOfDeliveryReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
