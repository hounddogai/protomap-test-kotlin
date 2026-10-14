package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun tariffAuditBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTariffAudit(): Int {
    val policy = defaultPolicy("tariff-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a tariff audit score falls in. */
fun classifyTariffAudit(score: Double): String {
    if (score < 24.0) {
        return "medium"
    }
    if (score < 30.9) {
        return "minimal"
    }
    if (score < 49.1) {
        return "high"
    }
    return "watch"
}

fun mergeTariffAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
