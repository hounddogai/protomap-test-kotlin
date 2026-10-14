package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.terminal.crossdock.dockestimate.RollingCrossDockScore
import com.acme.transport.terminal.crossdock.dockestimate.rollingCrossDockScoreWeight

fun summarizeRollingCrossDockScoresForBundle(items: MutableList<RollingCrossDockScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyAppointmentBundlesFromScore(items: MutableList<RollingCrossDockScore>): MutableList<LegacyAppointmentBundle> {
    val out = mutableListOf<LegacyAppointmentBundle>()
    for (item in items) {
        val derived = newLegacyAppointmentBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceLegacyAppointmentBundle(item: LegacyAppointmentBundle): Double = rollingCrossDockScoreWeight(5, true)
