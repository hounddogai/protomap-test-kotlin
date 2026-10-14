package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.freightpay.ratecard.segment.RollingRateCardSegment
import com.acme.transport.freightpay.ratecard.segment.normalizeRateCardManifestCode

fun summarizeRollingRateCardSegmentsForTier(items: MutableList<RollingRateCardSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveAuditTiersFromSegment(items: MutableList<RollingRateCardSegment>): MutableList<AuditTier> {
    val out = mutableListOf<AuditTier>()
    for (item in items) {
        val derived = newAuditTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitAuditTier(item: AuditTier): String = normalizeRateCardManifestCode(item.code)
