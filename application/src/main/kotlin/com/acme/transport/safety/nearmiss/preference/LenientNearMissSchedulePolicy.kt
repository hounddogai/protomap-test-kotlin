package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun lenientNearMissScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lenientNearMissScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientNearMissSchedule(): Int {
    val policy = defaultPolicy("lenient-near-miss-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
