package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.inspection.adjustment.InspectionRollup
import com.acme.transport.customs.inspection.adjustment.normalizeFallbackInspectionAdjustmentCode

fun summarizeInspectionRollupsForQuota(items: MutableList<InspectionRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveDeclarationQuotasFromRollup(items: MutableList<InspectionRollup>): MutableList<DeclarationQuota> {
    val out = mutableListOf<DeclarationQuota>()
    for (item in items) {
        val derived = newDeclarationQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileDeclarationQuota(item: DeclarationQuota): String {
    return normalizeFallbackInspectionAdjustmentCode(item.code)
}
