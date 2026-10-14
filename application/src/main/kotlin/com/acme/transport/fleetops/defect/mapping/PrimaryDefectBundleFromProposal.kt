package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.fleetops.inspection.summary.StrictInspectionProposal
import com.acme.transport.fleetops.inspection.summary.inspectionSummaryBucket

fun summarizeStrictInspectionProposalsForBundle(items: MutableList<StrictInspectionProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePrimaryDefectBundlesFromProposal(items: MutableList<StrictInspectionProposal>): MutableList<PrimaryDefectBundle> {
    val out = mutableListOf<PrimaryDefectBundle>()
    for (item in items) {
        val derived = newPrimaryDefectBundle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitPrimaryDefectBundle(item: PrimaryDefectBundle): String = inspectionSummaryBucket(item.netValue())
