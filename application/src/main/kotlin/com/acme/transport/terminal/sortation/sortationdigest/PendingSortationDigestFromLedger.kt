package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.terminal.sortation.digest.ScheduledSortationLedger
import com.acme.transport.terminal.sortation.digest.classifyScheduledSortationLedger

fun summarizeScheduledSortationLedgersForDigest(items: MutableList<ScheduledSortationLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingSortationDigestsFromLedger(items: MutableList<ScheduledSortationLedger>): MutableList<PendingSortationDigest> {
    val out = mutableListOf<PendingSortationDigest>()
    for (item in items) {
        val derived = newPendingSortationDigest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankPendingSortationDigest(item: PendingSortationDigest): String {
    return classifyScheduledSortationLedger(item.measuredValue())
}
