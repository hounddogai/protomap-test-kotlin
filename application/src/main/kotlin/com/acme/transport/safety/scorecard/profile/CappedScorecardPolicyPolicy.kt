package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun cappedScorecardPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedScorecardPolicy(): Int {
    val policy = defaultPolicy("capped-scorecard-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
