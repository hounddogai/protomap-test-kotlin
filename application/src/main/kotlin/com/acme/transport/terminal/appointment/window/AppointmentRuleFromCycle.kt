package com.acme.transport.terminal.appointment.window

import com.acme.transport.terminal.traileryard.cycle.LegacyTrailerYardCycle
import com.acme.transport.terminal.traileryard.cycle.normalizeLegacyTrailerYardCycleCode

fun summarizeLegacyTrailerYardCyclesForRule(items: MutableList<LegacyTrailerYardCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveAppointmentRulesFromCycle(items: MutableList<LegacyTrailerYardCycle>): MutableList<AppointmentRule> {
    val out = mutableListOf<AppointmentRule>()
    for (item in items) {
        val derived = newAppointmentRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeAppointmentRule(item: AppointmentRule): String = normalizeLegacyTrailerYardCycleCode(item.code)
