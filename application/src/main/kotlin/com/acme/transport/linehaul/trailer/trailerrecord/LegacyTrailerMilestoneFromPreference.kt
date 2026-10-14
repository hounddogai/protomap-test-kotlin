package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.linehaul.backhaul.backhaulaudit.LegacyBackhaulPreference
import com.acme.transport.linehaul.backhaul.backhaulaudit.cappedBackhaulLedgerWeight

fun summarizeLegacyBackhaulPreferencesForMilestone(items: MutableList<LegacyBackhaulPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLegacyTrailerMilestonesFromPreference(items: MutableList<LegacyBackhaulPreference>): MutableList<LegacyTrailerMilestone> {
    val out = mutableListOf<LegacyTrailerMilestone>()
    for (item in items) {
        val derived = newLegacyTrailerMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeLegacyTrailerMilestone(item: LegacyTrailerMilestone): Double = cappedBackhaulLedgerWeight(5, true)
