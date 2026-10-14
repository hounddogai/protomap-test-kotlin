package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.terminal.sortation.sortationtier.SortationAudit
import com.acme.transport.terminal.sortation.sortationtier.sortationProposalBucket

fun summarizeSortationAuditsForAssignment(items: MutableList<SortationAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveManualTrailerYardAssignmentsFromAudit(items: MutableList<SortationAudit>): MutableList<ManualTrailerYardAssignment> {
    val out = mutableListOf<ManualTrailerYardAssignment>()
    for (item in items) {
        val derived = newManualTrailerYardAssignment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveManualTrailerYardAssignment(item: ManualTrailerYardAssignment): String {
    return sortationProposalBucket(item.measuredValue())
}
