package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackCameraEventSummary(): Int {
    val policy = defaultPolicy("fallback-camera-event-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackCameraEventSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun fallbackCameraEventSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 34081.0, in order. */
fun fallbackCameraEventSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34081.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
