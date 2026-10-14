package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeScheduledSortationLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun scheduledSortationLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled sortation ledger score falls in. */
fun classifyScheduledSortationLedger(score: Double): String {
    if (score < 31.4) {
        return "medium"
    }
    if (score < 56.7) {
        return "high"
    }
    if (score < 62.5) {
        return "critical"
    }
    if (score < 82.8) {
        return "minimal"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledSortationLedger(): Int {
    val policy = defaultPolicy("scheduled-sortation-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
