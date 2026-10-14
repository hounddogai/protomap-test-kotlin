package com.acme.transport.terminal.appointment.group

import com.acme.transport.terminal.dwell.preference.ManualDwellEstimate
import com.acme.transport.terminal.dwell.preference.dwellPreferenceBucket

fun summarizeManualDwellEstimatesForGroup(items: MutableList<ManualDwellEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveFallbackAppointmentGroupsFromEstimate(items: MutableList<ManualDwellEstimate>): MutableList<FallbackAppointmentGroup> {
    val out = mutableListOf<FallbackAppointmentGroup>()
    for (item in items) {
        val derived = newFallbackAppointmentGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleFallbackAppointmentGroup(item: FallbackAppointmentGroup): String {
    return dwellPreferenceBucket(item.weightedLoad())
}
