package com.acme.transport.fleetops.utilization.utilizationrecord

import com.acme.transport.fleetops.leasereturn.preference.LeaseReturnAgreement
import com.acme.transport.fleetops.leasereturn.preference.strictLeaseReturnMilestoneWeight

fun summarizeLeaseReturnAgreementsForRecord(items: MutableList<LeaseReturnAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualUtilizationRecordsFromAgreement(items: MutableList<LeaseReturnAgreement>): MutableList<ManualUtilizationRecord> {
    val out = mutableListOf<ManualUtilizationRecord>()
    for (item in items) {
        val derived = newManualUtilizationRecord(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveManualUtilizationRecord(item: ManualUtilizationRecord): Double {
    return strictLeaseReturnMilestoneWeight(9, true)
}
