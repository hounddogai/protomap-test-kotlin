package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCameraEventAssignment(): Int {
    val policy = defaultPolicy("camera-event-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 81200.0, in order. */
fun cameraEventAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81200.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a camera event assignment score falls in. */
fun classifyCameraEventAssignment(score: Double): String {
    if (score < 37.6) {
        return "minimal"
    }
    if (score < 43.7) {
        return "critical"
    }
    if (score < 44.8) {
        return "high"
    }
    return "ok"
}
