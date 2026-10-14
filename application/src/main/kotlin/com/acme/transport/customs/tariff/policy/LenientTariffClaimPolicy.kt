package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a lenient tariff claim score falls in. */
fun classifyLenientTariffClaim(score: Double): String {
    if (score < 32.0) {
        return "critical"
    }
    if (score < 71.4) {
        return "ok"
    }
    if (score < 77.3) {
        return "medium"
    }
    if (score < 98.6) {
        return "high"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientTariffClaim(): Int {
    val policy = defaultPolicy("lenient-tariff-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeLenientTariffClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
