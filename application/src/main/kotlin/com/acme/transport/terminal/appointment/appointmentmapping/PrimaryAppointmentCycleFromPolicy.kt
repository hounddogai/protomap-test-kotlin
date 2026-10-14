package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.terminal.dwell.proposal.SeasonalDwellPolicy
import com.acme.transport.terminal.dwell.proposal.classifySeasonalDwellPolicy

fun summarizeSeasonalDwellPoliciesForCycle(items: MutableList<SeasonalDwellPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePrimaryAppointmentCyclesFromPolicy(items: MutableList<SeasonalDwellPolicy>): MutableList<PrimaryAppointmentCycle> {
    val out = mutableListOf<PrimaryAppointmentCycle>()
    for (item in items) {
        val derived = newPrimaryAppointmentCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitPrimaryAppointmentCycle(item: PrimaryAppointmentCycle): String {
    return classifySeasonalDwellPolicy(item.weightedTotal())
}
