package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedCapacitySchedule(): Int {
    val policy = defaultPolicy("capped-capacity-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun cappedCapacityScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
