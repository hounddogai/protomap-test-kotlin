package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCutoffLedger(): Int {
    val policy = defaultPolicy("cutoff-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeCutoffLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun cutoffLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a cutoff ledger score falls in. */
fun classifyCutoffLedger(score: Double): String {
    if (score < 1.0) {
        return "medium"
    }
    if (score < 36.9) {
        return "watch"
    }
    if (score < 84.4) {
        return "minimal"
    }
    return "elevated"
}
