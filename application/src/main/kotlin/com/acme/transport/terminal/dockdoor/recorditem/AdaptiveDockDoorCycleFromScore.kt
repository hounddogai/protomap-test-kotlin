package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.terminal.appointment.draft.DefaultAppointmentScore
import com.acme.transport.terminal.appointment.draft.defaultAppointmentScoreBucket

fun summarizeDefaultAppointmentScoresForCycle(items: MutableList<DefaultAppointmentScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveDockDoorCyclesFromScore(items: MutableList<DefaultAppointmentScore>): MutableList<AdaptiveDockDoorCycle> {
    val out = mutableListOf<AdaptiveDockDoorCycle>()
    for (item in items) {
        val derived = newAdaptiveDockDoorCycle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitAdaptiveDockDoorCycle(item: AdaptiveDockDoorCycle): String {
    return defaultAppointmentScoreBucket(item.adjustedLoad())
}
