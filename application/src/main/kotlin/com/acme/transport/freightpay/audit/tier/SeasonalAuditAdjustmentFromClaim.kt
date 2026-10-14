package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.dispute.recorditem.CappedDisputeClaim
import com.acme.transport.freightpay.dispute.recorditem.cappedDisputeClaimWeight

fun summarizeCappedDisputeClaimsForAdjustment(items: MutableList<CappedDisputeClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveSeasonalAuditAdjustmentsFromClaim(items: MutableList<CappedDisputeClaim>): MutableList<SeasonalAuditAdjustment> {
    val out = mutableListOf<SeasonalAuditAdjustment>()
    for (item in items) {
        val derived = newSeasonalAuditAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifySeasonalAuditAdjustment(item: SeasonalAuditAdjustment): Double = cappedDisputeClaimWeight(2, true)
