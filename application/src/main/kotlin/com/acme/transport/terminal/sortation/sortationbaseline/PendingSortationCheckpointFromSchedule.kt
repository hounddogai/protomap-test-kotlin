package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.terminal.dwell.adjustment.LenientDwellSchedule
import com.acme.transport.terminal.dwell.adjustment.lenientDwellScheduleBucket

fun summarizeLenientDwellSchedulesForCheckpoint(items: MutableList<LenientDwellSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun derivePendingSortationCheckpointsFromSchedule(items: MutableList<LenientDwellSchedule>): MutableList<PendingSortationCheckpoint> {
    val out = mutableListOf<PendingSortationCheckpoint>()
    for (item in items) {
        val derived = newPendingSortationCheckpoint(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankPendingSortationCheckpoint(item: PendingSortationCheckpoint): String {
    return lenientDwellScheduleBucket(item.measuredValue())
}
