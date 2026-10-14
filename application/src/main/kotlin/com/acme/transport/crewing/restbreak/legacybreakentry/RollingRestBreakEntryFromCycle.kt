package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.qualification.qualificationcycle.SeasonalQualificationCycle
import com.acme.transport.crewing.qualification.qualificationcycle.normalizeLegacyQualificationReviewCode

fun summarizeSeasonalQualificationCyclesForEntry(items: MutableList<SeasonalQualificationCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingRestBreakEntriesFromCycle(items: MutableList<SeasonalQualificationCycle>): MutableList<RollingRestBreakEntry> {
    val out = mutableListOf<RollingRestBreakEntry>()
    for (item in items) {
        val derived = newRollingRestBreakEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRollingRestBreakEntry(item: RollingRestBreakEntry): String {
    return normalizeLegacyQualificationReviewCode(item.code)
}
