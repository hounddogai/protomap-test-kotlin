package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.terminal.appointment.segment.SeasonalAppointmentAudit
import com.acme.transport.terminal.appointment.segment.classifyGlobalAppointmentBudget

fun summarizeSeasonalAppointmentAuditsForOverride(items: MutableList<SeasonalAppointmentAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDwellOverridesFromAudit(items: MutableList<SeasonalAppointmentAudit>): MutableList<DwellOverride> {
    val out = mutableListOf<DwellOverride>()
    for (item in items) {
        val derived = newDwellOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchDwellOverride(item: DwellOverride): String = classifyGlobalAppointmentBudget(item.adjustedLoad())
