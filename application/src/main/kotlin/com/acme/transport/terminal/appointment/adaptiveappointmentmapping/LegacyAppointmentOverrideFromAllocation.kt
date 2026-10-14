package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.terminal.dwell.exception.DwellAllocation
import com.acme.transport.terminal.dwell.exception.classifyDwellAllocation

fun summarizeDwellAllocationsForOverride(items: MutableList<DwellAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveLegacyAppointmentOverridesFromAllocation(items: MutableList<DwellAllocation>): MutableList<LegacyAppointmentOverride> {
    val out = mutableListOf<LegacyAppointmentOverride>()
    for (item in items) {
        val derived = newLegacyAppointmentOverride(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLegacyAppointmentOverride(item: LegacyAppointmentOverride): String {
    return classifyDwellAllocation(item.measuredValue())
}
