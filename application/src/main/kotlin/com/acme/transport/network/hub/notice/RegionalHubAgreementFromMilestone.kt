package com.acme.transport.network.hub.notice

import com.acme.transport.network.cutoff.allocation.CutoffMilestone
import com.acme.transport.network.cutoff.allocation.cutoffAdjustmentWeight

fun summarizeCutoffMilestonesForAgreement(items: MutableList<CutoffMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveRegionalHubAgreementsFromMilestone(items: MutableList<CutoffMilestone>): MutableList<RegionalHubAgreement> {
    val out = mutableListOf<RegionalHubAgreement>()
    for (item in items) {
        val derived = newRegionalHubAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRegionalHubAgreement(item: RegionalHubAgreement): Double = cutoffAdjustmentWeight(2, true)
