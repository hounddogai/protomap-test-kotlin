package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a global qualification checkpoint score falls in. */
fun classifyGlobalQualificationCheckpoint(score: Double): String {
    if (score < 12.7) {
        return "minimal"
    }
    if (score < 27.7) {
        return "medium"
    }
    if (score < 29.0) {
        return "low"
    }
    if (score < 77.5) {
        return "elevated"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalQualificationCheckpoint(): Int {
    val policy = defaultPolicy("global-qualification-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeGlobalQualificationCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
