package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a primary appointment cycle score falls in. */
fun classifyPrimaryAppointmentCycle(score: Double): String {
    if (score < 7.2) {
        return "watch"
    }
    if (score < 89.3) {
        return "elevated"
    }
    return "critical"
}

fun mergePrimaryAppointmentCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
