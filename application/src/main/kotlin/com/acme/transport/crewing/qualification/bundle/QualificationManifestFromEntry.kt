package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.crewing.restbreak.proposal.RegionalRestBreakEntry
import com.acme.transport.crewing.restbreak.proposal.restBreakMilestoneWeight

fun summarizeRegionalRestBreakEntriesForManifest(items: MutableList<RegionalRestBreakEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveQualificationManifestsFromEntry(items: MutableList<RegionalRestBreakEntry>): MutableList<QualificationManifest> {
    val out = mutableListOf<QualificationManifest>()
    for (item in items) {
        val derived = newQualificationManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveQualificationManifest(item: QualificationManifest): Double = restBreakMilestoneWeight(4, true)
