package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 89768.0, in order. */
fun adaptiveCameraEventRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89768.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a adaptive camera event record score falls in. */
fun classifyAdaptiveCameraEventRecord(score: Double): String {
    if (score < 30.3) {
        return "ok"
    }
    if (score < 38.2) {
        return "medium"
    }
    if (score < 98.1) {
        return "minimal"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveCameraEventRecord(): Int {
    val policy = defaultPolicy("adaptive-camera-event-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveCameraEventRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.08
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
