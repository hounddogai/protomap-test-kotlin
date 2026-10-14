package com.acme.transport.terminal.appointment.preference

import com.acme.transport.terminal.sortation.variance.SortationRollup
import com.acme.transport.terminal.sortation.variance.normalizeSortationVarianceCode

fun summarizeSortationRollupsForScore(items: MutableList<SortationRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAppointmentScoresFromRollup(items: MutableList<SortationRollup>): MutableList<AppointmentScore> {
    val out = mutableListOf<AppointmentScore>()
    for (item in items) {
        val derived = newAppointmentScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleAppointmentScore(item: AppointmentScore): String = normalizeSortationVarianceCode(item.code)
