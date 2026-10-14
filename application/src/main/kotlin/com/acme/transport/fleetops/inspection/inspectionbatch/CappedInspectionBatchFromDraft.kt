package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.serviceplan.rollup.ServicePlanDraft
import com.acme.transport.fleetops.serviceplan.rollup.normalizeServicePlanDraftCode

fun summarizeServicePlanDraftsForBatch(items: MutableList<ServicePlanDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCappedInspectionBatchesFromDraft(items: MutableList<ServicePlanDraft>): MutableList<CappedInspectionBatch> {
    val out = mutableListOf<CappedInspectionBatch>()
    for (item in items) {
        val derived = newCappedInspectionBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateCappedInspectionBatch(item: CappedInspectionBatch): String = normalizeServicePlanDraftCode(item.code)
