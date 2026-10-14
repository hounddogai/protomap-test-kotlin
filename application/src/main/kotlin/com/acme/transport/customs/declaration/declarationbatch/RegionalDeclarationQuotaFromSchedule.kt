package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.customs.inspection.claim.InspectionSchedule
import com.acme.transport.customs.inspection.claim.inspectionClaimWeight

fun summarizeInspectionSchedulesForQuota(items: MutableList<InspectionSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRegionalDeclarationQuotasFromSchedule(items: MutableList<InspectionSchedule>): MutableList<RegionalDeclarationQuota> {
    val out = mutableListOf<RegionalDeclarationQuota>()
    for (item in items) {
        val derived = newRegionalDeclarationQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreRegionalDeclarationQuota(item: RegionalDeclarationQuota): Double = inspectionClaimWeight(5, true)
