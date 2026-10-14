package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.emissions.idling.idlingwindow.ScheduledIdlingWindow
import com.acme.transport.emissions.idling.idlingwindow.scheduledIdlingWindowBucket

fun summarizeScheduledIdlingWindowsForSummary(items: MutableList<ScheduledIdlingWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveStrictEvChargeSummariesFromWindow(items: MutableList<ScheduledIdlingWindow>): MutableList<StrictEvChargeSummary> {
    val out = mutableListOf<StrictEvChargeSummary>()
    for (item in items) {
        val derived = newStrictEvChargeSummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankStrictEvChargeSummary(item: StrictEvChargeSummary): String {
    return scheduledIdlingWindowBucket(item.projectedVolume())
}
