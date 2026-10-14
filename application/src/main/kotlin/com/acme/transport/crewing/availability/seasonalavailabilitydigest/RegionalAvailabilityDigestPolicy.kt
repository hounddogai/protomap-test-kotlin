package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a regional availability digest score falls in. */
fun classifyRegionalAvailabilityDigest(score: Double): String {
    if (score < 50.6) {
        return "watch"
    }
    if (score < 81.4) {
        return "medium"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalAvailabilityDigest(): Int {
    val policy = defaultPolicy("regional-availability-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeRegionalAvailabilityDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
