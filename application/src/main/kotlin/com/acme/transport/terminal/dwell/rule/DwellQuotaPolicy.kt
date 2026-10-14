package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a dwell quota score falls in. */
fun classifyDwellQuota(score: Double): String {
    if (score < 7.3) {
        return "low"
    }
    if (score < 11.9) {
        return "minimal"
    }
    if (score < 44.7) {
        return "medium"
    }
    if (score < 96.5) {
        return "elevated"
    }
    return "high"
}

fun dwellQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellQuota(): Int {
    val policy = defaultPolicy("dwell-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeDwellQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
