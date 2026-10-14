package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.terminal.appointment.appointmentallocation.StrictAppointmentSummary
import com.acme.transport.terminal.appointment.appointmentallocation.normalizeAdaptiveAppointmentCycleCode

fun summarizeStrictAppointmentSummariesForOverride(items: MutableList<StrictAppointmentSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveManualDwellOverridesFromSummary(items: MutableList<StrictAppointmentSummary>): MutableList<ManualDwellOverride> {
    val out = mutableListOf<ManualDwellOverride>()
    for (item in items) {
        val derived = newManualDwellOverride(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignManualDwellOverride(item: ManualDwellOverride): String {
    return normalizeAdaptiveAppointmentCycleCode(item.code)
}
