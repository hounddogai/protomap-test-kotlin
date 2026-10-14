package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a tiered trailer template score falls in. */
fun classifyTieredTrailerTemplate(score: Double): String {
    if (score < 4.3) {
        return "low"
    }
    if (score < 16.1) {
        return "high"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredTrailerTemplate(): Int {
    val policy = defaultPolicy("tiered-trailer-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeTieredTrailerTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
