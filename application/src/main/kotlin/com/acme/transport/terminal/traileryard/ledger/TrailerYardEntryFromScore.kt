package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.terminal.appointment.draft.DefaultAppointmentScore
import com.acme.transport.terminal.appointment.draft.defaultAppointmentScoreBucket

fun summarizeDefaultAppointmentScoresForEntry(items: MutableList<DefaultAppointmentScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTrailerYardEntriesFromScore(items: MutableList<DefaultAppointmentScore>): MutableList<TrailerYardEntry> {
    val out = mutableListOf<TrailerYardEntry>()
    for (item in items) {
        val derived = newTrailerYardEntry(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateTrailerYardEntry(item: TrailerYardEntry): String {
    return defaultAppointmentScoreBucket(item.adjustedVolume())
}
