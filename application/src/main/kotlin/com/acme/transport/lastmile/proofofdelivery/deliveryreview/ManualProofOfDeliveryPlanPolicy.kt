package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeManualProofOfDeliveryPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualProofOfDeliveryPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a manual proof of delivery plan score falls in. */
fun classifyManualProofOfDeliveryPlan(score: Double): String {
    if (score < 4.3) {
        return "medium"
    }
    if (score < 5.5) {
        return "low"
    }
    if (score < 55.1) {
        return "ok"
    }
    return "watch"
}

fun manualProofOfDeliveryPlanBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
