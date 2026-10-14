package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a camera event allocation code in its canonical CEA- form. */
fun normalizeCameraEventAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEA-")) {
        return clean
    }
    return "CEA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCameraEventAllocation(): Int {
    val policy = defaultPolicy("camera-event-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun cameraEventAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
