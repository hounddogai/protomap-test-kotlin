package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a default attempt digest score falls in. */
fun classifyDefaultAttemptDigest(score: Double): String {
    if (score < 70.2) {
        return "ok"
    }
    if (score < 78.3) {
        return "high"
    }
    if (score < 90.2) {
        return "watch"
    }
    return "minimal"
}

fun mergeDefaultAttemptDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultAttemptDigest(): Int {
    val policy = defaultPolicy("default-attempt-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
