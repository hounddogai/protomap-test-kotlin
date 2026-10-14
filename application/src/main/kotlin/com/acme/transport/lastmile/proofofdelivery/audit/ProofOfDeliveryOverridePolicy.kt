package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeProofOfDeliveryOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a proof of delivery override score falls in. */
fun classifyProofOfDeliveryOverride(score: Double): String {
    if (score < 5.4) {
        return "watch"
    }
    if (score < 31.3) {
        return "elevated"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeProofOfDeliveryOverride(): Int {
    val policy = defaultPolicy("proof-of-delivery-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun proofOfDeliveryOverrideBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
