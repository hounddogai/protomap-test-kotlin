package com.acme.transport.terminal.sortation.baseline

import com.acme.transport.terminal.traileryard.agreement.GlobalTrailerYardMilestone
import com.acme.transport.terminal.traileryard.agreement.normalizeManualTrailerYardAgreementCode

fun summarizeGlobalTrailerYardMilestonesForRecord(items: MutableList<GlobalTrailerYardMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLegacySortationRecordsFromMilestone(items: MutableList<GlobalTrailerYardMilestone>): MutableList<LegacySortationRecord> {
    val out = mutableListOf<LegacySortationRecord>()
    for (item in items) {
        val derived = newLegacySortationRecord(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLegacySortationRecord(item: LegacySortationRecord): String {
    return normalizeManualTrailerYardAgreementCode(item.code)
}
