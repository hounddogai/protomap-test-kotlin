package com.acme.transport.freightpay.audit.notice

import com.acme.transport.freightpay.ratecard.group.LegacyRateCardAssignment
import com.acme.transport.freightpay.ratecard.group.rateCardAssignmentBucket

fun summarizeLegacyRateCardAssignmentsForQuota(items: MutableList<LegacyRateCardAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAuditQuotasFromAssignment(items: MutableList<LegacyRateCardAssignment>): MutableList<AuditQuota> {
    val out = mutableListOf<AuditQuota>()
    for (item in items) {
        val derived = newAuditQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAuditQuota(item: AuditQuota): String = rateCardAssignmentBucket(item.adjustedExposure())
