package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientLockerException(): Int {
    val policy = defaultPolicy("lenient-locker-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lenient locker exception score falls in. */
fun classifyLenientLockerException(score: Double): String {
    if (score < 40.3) {
        return "low"
    }
    if (score < 65.1) {
        return "minimal"
    }
    if (score < 87.9) {
        return "watch"
    }
    return "elevated"
}
