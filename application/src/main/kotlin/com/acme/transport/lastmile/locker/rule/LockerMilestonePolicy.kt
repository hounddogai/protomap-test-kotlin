package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a locker milestone score falls in. */
fun classifyLockerMilestone(score: Double): String {
    if (score < 34.9) {
        return "ok"
    }
    if (score < 82.9) {
        return "watch"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLockerMilestone(): Int {
    val policy = defaultPolicy("locker-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
