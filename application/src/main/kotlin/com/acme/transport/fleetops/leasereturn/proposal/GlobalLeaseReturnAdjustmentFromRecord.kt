package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.defect.recorditem.DefectRecord
import com.acme.transport.fleetops.defect.recorditem.classifyLegacyDefectAgreement

fun summarizeDefectRecordsForAdjustment(items: MutableList<DefectRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveGlobalLeaseReturnAdjustmentsFromRecord(items: MutableList<DefectRecord>): MutableList<GlobalLeaseReturnAdjustment> {
    val out = mutableListOf<GlobalLeaseReturnAdjustment>()
    for (item in items) {
        val derived = newGlobalLeaseReturnAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchGlobalLeaseReturnAdjustment(item: GlobalLeaseReturnAdjustment): String {
    return classifyLegacyDefectAgreement(item.measuredValue())
}
