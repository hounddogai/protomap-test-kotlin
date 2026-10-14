package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun seasonalAppointmentDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal appointment digest score falls in. */
fun classifySeasonalAppointmentDigest(score: Double): String {
    if (score < 3.0) {
        return "minimal"
    }
    if (score < 28.1) {
        return "low"
    }
    if (score < 51.4) {
        return "ok"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalAppointmentDigest(): Int {
    val policy = defaultPolicy("seasonal-appointment-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeSeasonalAppointmentDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
