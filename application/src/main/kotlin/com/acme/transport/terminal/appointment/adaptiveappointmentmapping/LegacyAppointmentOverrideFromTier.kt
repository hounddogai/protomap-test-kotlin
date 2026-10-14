package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.terminal.appointment.regionalappointmentmapping.AppointmentTier
import com.acme.transport.terminal.appointment.regionalappointmentmapping.appointmentRequestBucket

fun summarizeAppointmentTiersForOverride(items: MutableList<AppointmentTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLegacyAppointmentOverridesFromTier(items: MutableList<AppointmentTier>): MutableList<LegacyAppointmentOverride> {
    val out = mutableListOf<LegacyAppointmentOverride>()
    for (item in items) {
        val derived = newLegacyAppointmentOverride(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyLegacyAppointmentOverride(item: LegacyAppointmentOverride): String {
    return appointmentRequestBucket(item.measuredValue())
}
