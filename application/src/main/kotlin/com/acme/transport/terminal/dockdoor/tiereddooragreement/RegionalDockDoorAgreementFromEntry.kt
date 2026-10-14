package com.acme.transport.terminal.dockdoor.tiereddooragreement

import com.acme.transport.terminal.traileryard.yardentry.ScheduledTrailerYardEntry
import com.acme.transport.terminal.traileryard.yardentry.lenientTrailerYardSegmentBucket

fun summarizeScheduledTrailerYardEntriesForAgreement(items: MutableList<ScheduledTrailerYardEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalDockDoorAgreementsFromEntry(items: MutableList<ScheduledTrailerYardEntry>): MutableList<RegionalDockDoorAgreement> {
    val out = mutableListOf<RegionalDockDoorAgreement>()
    for (item in items) {
        val derived = newRegionalDockDoorAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRegionalDockDoorAgreement(item: RegionalDockDoorAgreement): String {
    return lenientTrailerYardSegmentBucket(item.projectedTotal())
}
