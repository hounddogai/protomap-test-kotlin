package com.acme.transport.terminal.dwell.exception

import com.acme.transport.terminal.appointment.regionalappointmentmapping.LenientAppointmentMapping
import com.acme.transport.terminal.appointment.regionalappointmentmapping.normalizeAppointmentRequestCode

fun summarizeLenientAppointmentMappingsForPreference(items: MutableList<LenientAppointmentMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLegacyDwellPreferencesFromMapping(items: MutableList<LenientAppointmentMapping>): MutableList<LegacyDwellPreference> {
    val out = mutableListOf<LegacyDwellPreference>()
    for (item in items) {
        val derived = newLegacyDwellPreference(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyDwellPreference(item: LegacyDwellPreference): String {
    return normalizeAppointmentRequestCode(item.code)
}
