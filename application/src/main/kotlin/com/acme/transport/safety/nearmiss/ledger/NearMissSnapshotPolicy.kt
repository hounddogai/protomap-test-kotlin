package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a near miss snapshot score falls in. */
fun classifyNearMissSnapshot(score: Double): String {
    if (score < 5.4) {
        return "minimal"
    }
    if (score < 13.8) {
        return "high"
    }
    if (score < 30.2) {
        return "critical"
    }
    if (score < 31.9) {
        return "watch"
    }
    return "ok"
}

fun mergeNearMissSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNearMissSnapshot(): Int {
    val policy = defaultPolicy("near-miss-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun nearMissSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
