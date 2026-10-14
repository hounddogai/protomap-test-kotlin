package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.terminal.dockdoor.dooragreement.DockDoorPolicy
import com.acme.transport.terminal.dockdoor.dooragreement.classifyDockDoorPolicy

fun summarizeDockDoorPoliciesForVariance(items: MutableList<DockDoorPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalAppointmentVariancesFromPolicy(items: MutableList<DockDoorPolicy>): MutableList<RegionalAppointmentVariance> {
    val out = mutableListOf<RegionalAppointmentVariance>()
    for (item in items) {
        val derived = newRegionalAppointmentVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRegionalAppointmentVariance(item: RegionalAppointmentVariance): String {
    return classifyDockDoorPolicy(item.effectiveValue())
}
