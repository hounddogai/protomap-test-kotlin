package com.acme.transport.terminal.appointment.draft

import com.acme.transport.terminal.crossdock.profile.LegacyCrossDockProfile
import com.acme.transport.terminal.crossdock.profile.classifyLegacyCrossDockProfile

fun summarizeLegacyCrossDockProfilesForScore(items: MutableList<LegacyCrossDockProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultAppointmentScoresFromProfile(items: MutableList<LegacyCrossDockProfile>): MutableList<DefaultAppointmentScore> {
    val out = mutableListOf<DefaultAppointmentScore>()
    for (item in items) {
        val derived = newDefaultAppointmentScore(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateDefaultAppointmentScore(item: DefaultAppointmentScore): String {
    return classifyLegacyCrossDockProfile(item.measuredValue())
}
