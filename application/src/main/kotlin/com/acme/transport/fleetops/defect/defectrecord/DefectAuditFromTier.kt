package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.fleetops.tire.overrideitem.BatchedTireTier
import com.acme.transport.fleetops.tire.overrideitem.normalizeBatchedTireTierCode

fun summarizeBatchedTireTiersForAudit(items: MutableList<BatchedTireTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDefectAuditsFromTier(items: MutableList<BatchedTireTier>): MutableList<DefectAudit> {
    val out = mutableListOf<DefectAudit>()
    for (item in items) {
        val derived = newDefectAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeDefectAudit(item: DefectAudit): String = normalizeBatchedTireTierCode(item.code)
