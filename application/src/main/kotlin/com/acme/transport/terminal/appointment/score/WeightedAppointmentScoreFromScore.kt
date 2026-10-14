package com.acme.transport.terminal.appointment.score

import com.acme.transport.terminal.sortation.score.SortationScore
import com.acme.transport.terminal.sortation.score.sortationScoreBucket

fun summarizeSortationScoresForScore(items: MutableList<SortationScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedAppointmentScoresFromScore(items: MutableList<SortationScore>): MutableList<WeightedAppointmentScore> {
    val out = mutableListOf<WeightedAppointmentScore>()
    for (item in items) {
        val derived = newWeightedAppointmentScore(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreWeightedAppointmentScore(item: WeightedAppointmentScore): String {
    return sortationScoreBucket(item.adjustedTotal())
}
