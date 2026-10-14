package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.freightpay.ratecard.cardgroup.StrictRateCardAssignment
import com.acme.transport.freightpay.ratecard.cardgroup.strictRateCardAssignmentBucket

fun summarizeStrictRateCardAssignmentsForAdjustment(items: MutableList<StrictRateCardAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveGlobalAuditAdjustmentsFromAssignment(items: MutableList<StrictRateCardAssignment>): MutableList<GlobalAuditAdjustment> {
    val out = mutableListOf<GlobalAuditAdjustment>()
    for (item in items) {
        val derived = newGlobalAuditAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeGlobalAuditAdjustment(item: GlobalAuditAdjustment): String {
    return strictRateCardAssignmentBucket(item.weightedTotal())
}
