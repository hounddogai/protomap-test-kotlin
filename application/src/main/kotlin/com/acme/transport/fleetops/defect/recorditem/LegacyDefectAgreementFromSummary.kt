package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.fleetops.tire.budget.CappedTireSummary
import com.acme.transport.fleetops.tire.budget.rollingTireTargetBucket

fun summarizeCappedTireSummariesForAgreement(items: MutableList<CappedTireSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveLegacyDefectAgreementsFromSummary(items: MutableList<CappedTireSummary>): MutableList<LegacyDefectAgreement> {
    val out = mutableListOf<LegacyDefectAgreement>()
    for (item in items) {
        val derived = newLegacyDefectAgreement(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyLegacyDefectAgreementExtra(item: LegacyDefectAgreement): String {
    return rollingTireTargetBucket(item.projectedExposure())
}
