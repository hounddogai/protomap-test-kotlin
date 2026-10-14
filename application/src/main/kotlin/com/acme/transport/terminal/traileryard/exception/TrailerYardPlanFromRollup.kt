package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.terminal.appointment.bundle.LenientAppointmentRollup
import com.acme.transport.terminal.appointment.bundle.classifyDefaultAppointmentManifest

fun summarizeLenientAppointmentRollupsForPlan(items: MutableList<LenientAppointmentRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveTrailerYardPlansFromRollup(items: MutableList<LenientAppointmentRollup>): MutableList<TrailerYardPlan> {
    val out = mutableListOf<TrailerYardPlan>()
    for (item in items) {
        val derived = newTrailerYardPlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeTrailerYardPlan(item: TrailerYardPlan): String {
    return classifyDefaultAppointmentManifest(item.measuredValue())
}
