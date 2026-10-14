package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.terminal.appointment.appointmentpreference.AppointmentDigest
import com.acme.transport.terminal.appointment.appointmentpreference.normalizeAppointmentManifestCode

fun summarizeAppointmentDigestsForAudit(items: MutableList<AppointmentDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientAppointmentAuditsFromDigest(items: MutableList<AppointmentDigest>): MutableList<LenientAppointmentAudit> {
    val out = mutableListOf<LenientAppointmentAudit>()
    for (item in items) {
        val derived = newLenientAppointmentAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveLenientAppointmentAudit(item: LenientAppointmentAudit): String {
    return normalizeAppointmentManifestCode(item.code)
}
