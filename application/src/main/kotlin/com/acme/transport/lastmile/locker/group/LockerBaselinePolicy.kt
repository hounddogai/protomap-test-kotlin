package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLockerBaseline(): Int {
    val policy = defaultPolicy("locker-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lockerBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun lockerBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
