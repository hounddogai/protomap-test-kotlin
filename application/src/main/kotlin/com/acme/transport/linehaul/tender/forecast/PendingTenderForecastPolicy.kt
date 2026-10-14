package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun pendingTenderForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun mergePendingTenderForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending tender forecast score falls in. */
fun classifyPendingTenderForecast(score: Double): String {
    if (score < 38.0) {
        return "low"
    }
    if (score < 43.1) {
        return "critical"
    }
    if (score < 51.4) {
        return "high"
    }
    if (score < 60.0) {
        return "ok"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingTenderForecast(): Int {
    val policy = defaultPolicy("pending-tender-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
