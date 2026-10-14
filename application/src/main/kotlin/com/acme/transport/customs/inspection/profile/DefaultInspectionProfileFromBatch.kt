package com.acme.transport.customs.inspection.profile

import com.acme.transport.customs.clearance.rule.ClearanceBatch
import com.acme.transport.customs.clearance.rule.normalizeClearanceBatchCode

fun summarizeClearanceBatchesForProfile(items: MutableList<ClearanceBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDefaultInspectionProfilesFromBatch(items: MutableList<ClearanceBatch>): MutableList<DefaultInspectionProfile> {
    val out = mutableListOf<DefaultInspectionProfile>()
    for (item in items) {
        val derived = newDefaultInspectionProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectDefaultInspectionProfile(item: DefaultInspectionProfile): String {
    return normalizeClearanceBatchCode(item.code)
}
