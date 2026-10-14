package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.fleetops.defect.target.DefectRollup
import com.acme.transport.fleetops.defect.target.defectTargetBucket

fun summarizeDefectRollupsForBaseline(items: MutableList<DefectRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveLeaseReturnBaselinesFromRollup(items: MutableList<DefectRollup>): MutableList<LeaseReturnBaseline> {
    val out = mutableListOf<LeaseReturnBaseline>()
    for (item in items) {
        val derived = newLeaseReturnBaseline(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleLeaseReturnBaseline(item: LeaseReturnBaseline): String = defectTargetBucket(item.netExposure())
