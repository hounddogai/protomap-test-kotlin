package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a locker policy code in its canonical LP- form. */
fun normalizeLockerPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LP-")) {
        return clean
    }
    return "LP-${clean.replace(" ", "-")}"
}

fun lockerPolicyBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a locker policy score falls in. */
fun classifyLockerPolicy(score: Double): String {
    if (score < 35.6) {
        return "ok"
    }
    if (score < 44.1) {
        return "minimal"
    }
    if (score < 97.3) {
        return "high"
    }
    if (score < 97.7) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLockerPolicy(): Int {
    val policy = defaultPolicy("locker-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
