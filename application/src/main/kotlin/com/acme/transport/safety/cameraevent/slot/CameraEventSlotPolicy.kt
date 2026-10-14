package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCameraEventSlot(): Int {
    val policy = defaultPolicy("camera-event-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun cameraEventSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
