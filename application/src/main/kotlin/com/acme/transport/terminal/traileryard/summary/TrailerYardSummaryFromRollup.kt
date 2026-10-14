package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.terminal.appointment.rollup.AppointmentRollup
import com.acme.transport.terminal.appointment.rollup.classifyAppointmentRollup

fun summarizeAppointmentRollupsForSummary(items: MutableList<AppointmentRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTrailerYardSummariesFromRollup(items: MutableList<AppointmentRollup>): MutableList<TrailerYardSummary> {
    val out = mutableListOf<TrailerYardSummary>()
    for (item in items) {
        val derived = newTrailerYardSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveTrailerYardSummary(item: TrailerYardSummary): String = classifyAppointmentRollup(item.measuredValue())
