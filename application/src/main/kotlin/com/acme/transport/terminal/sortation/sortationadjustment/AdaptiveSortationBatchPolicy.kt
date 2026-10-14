package com.acme.transport.terminal.sortation.sortationadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a adaptive sortation batch score falls in. */
fun classifyAdaptiveSortationBatch(score: Double): String {
    if (score < 21.4) {
        return "watch"
    }
    if (score < 94.8) {
        return "low"
    }
    return "high"
}

fun mergeAdaptiveSortationBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveSortationBatch(): Int {
    val policy = defaultPolicy("adaptive-sortation-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
