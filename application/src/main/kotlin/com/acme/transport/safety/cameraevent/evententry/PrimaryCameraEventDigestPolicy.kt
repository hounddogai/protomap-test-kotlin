package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun primaryCameraEventDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryCameraEventDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryCameraEventDigest(): Int {
    val policy = defaultPolicy("primary-camera-event-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 90343.0, in order. */
fun primaryCameraEventDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90343.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
