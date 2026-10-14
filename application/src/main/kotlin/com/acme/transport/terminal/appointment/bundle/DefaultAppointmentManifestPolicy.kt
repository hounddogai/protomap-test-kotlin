package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultAppointmentManifest(): Int {
    val policy = defaultPolicy("default-appointment-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultAppointmentManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a default appointment manifest score falls in. */
fun classifyDefaultAppointmentManifest(score: Double): String {
    if (score < 11.7) {
        return "minimal"
    }
    if (score < 34.9) {
        return "critical"
    }
    if (score < 45.2) {
        return "high"
    }
    return "watch"
}

fun mergeDefaultAppointmentManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
