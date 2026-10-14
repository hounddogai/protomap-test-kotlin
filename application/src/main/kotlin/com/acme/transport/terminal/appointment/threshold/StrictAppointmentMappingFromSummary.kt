package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.terminal.crossdock.summary.LegacyCrossDockSummary
import com.acme.transport.terminal.crossdock.summary.classifyLegacyCrossDockSummary

fun summarizeLegacyCrossDockSummariesForMapping(items: MutableList<LegacyCrossDockSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveStrictAppointmentMappingsFromSummary(items: MutableList<LegacyCrossDockSummary>): MutableList<StrictAppointmentMapping> {
    val out = mutableListOf<StrictAppointmentMapping>()
    for (item in items) {
        val derived = newStrictAppointmentMapping(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyStrictAppointmentMapping(item: StrictAppointmentMapping): String {
    return classifyLegacyCrossDockSummary(item.adjustedExposure())
}
