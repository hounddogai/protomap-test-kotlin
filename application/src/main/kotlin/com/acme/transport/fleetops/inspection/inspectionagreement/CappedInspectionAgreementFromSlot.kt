package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.fleetops.defect.defectmapping.PendingDefectSlot
import com.acme.transport.fleetops.defect.defectmapping.pendingDefectSlotWeight

fun summarizePendingDefectSlotsForAgreement(items: MutableList<PendingDefectSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCappedInspectionAgreementsFromSlot(items: MutableList<PendingDefectSlot>): MutableList<CappedInspectionAgreement> {
    val out = mutableListOf<CappedInspectionAgreement>()
    for (item in items) {
        val derived = newCappedInspectionAgreement(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeCappedInspectionAgreement(item: CappedInspectionAgreement): Double = pendingDefectSlotWeight(3, true)
