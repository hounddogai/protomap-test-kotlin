package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun cappedHubMappingBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a capped hub mapping score falls in. */
fun classifyCappedHubMapping(score: Double): String {
    if (score < 7.2) {
        return "medium"
    }
    if (score < 58.3) {
        return "critical"
    }
    if (score < 70.0) {
        return "low"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedHubMapping(): Int {
    val policy = defaultPolicy("capped-hub-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
