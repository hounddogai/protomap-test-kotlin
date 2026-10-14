package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledAppointmentForecast(): Int {
    val policy = defaultPolicy("scheduled-appointment-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a scheduled appointment forecast code in its canonical SAF- form. */
fun normalizeScheduledAppointmentForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAF-")) {
        return clean
    }
    return "SAF-${clean.replace(" ", "-")}"
}

fun mergeScheduledAppointmentForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
