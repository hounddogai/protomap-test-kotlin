package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.freightpay.audit.auditclaim.CappedAuditTarget
import com.acme.transport.freightpay.audit.auditclaim.classifyRollingAuditClaim

fun summarizeCappedAuditTargetsForMilestone(items: MutableList<CappedAuditTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedFuelSurchargeMilestonesFromTarget(items: MutableList<CappedAuditTarget>): MutableList<BatchedFuelSurchargeMilestone> {
    val out = mutableListOf<BatchedFuelSurchargeMilestone>()
    for (item in items) {
        val derived = newBatchedFuelSurchargeMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyBatchedFuelSurchargeMilestone(item: BatchedFuelSurchargeMilestone): String {
    return classifyRollingAuditClaim(item.projectedValue())
}
