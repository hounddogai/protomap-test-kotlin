package com.acme.transport.terminal.appointment.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualAppointmentWindow(): Int {
    val policy = defaultPolicy("manual-appointment-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual appointment window score falls in. */
fun classifyManualAppointmentWindow(score: Double): String {
    if (score < 3.2) {
        return "medium"
    }
    if (score < 32.3) {
        return "low"
    }
    if (score < 34.9) {
        return "high"
    }
    if (score < 96.2) {
        return "minimal"
    }
    return "watch"
}

fun mergeManualAppointmentWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualAppointmentWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
