package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveDropLedger(): Int {
    val policy = defaultPolicy("adaptive-drop-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive drop ledger score falls in. */
fun classifyAdaptiveDropLedger(score: Double): String {
    if (score < 2.8) {
        return "low"
    }
    if (score < 96.8) {
        return "ok"
    }
    if (score < 97.9) {
        return "watch"
    }
    return "medium"
}

fun mergeAdaptiveDropLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
