package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryAppointmentQuota(): Int {
    val policy = defaultPolicy("primary-appointment-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary appointment quota score falls in. */
fun classifyPrimaryAppointmentQuota(score: Double): String {
    if (score < 64.1) {
        return "high"
    }
    if (score < 95.4) {
        return "minimal"
    }
    return "low"
}

fun mergePrimaryAppointmentQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
