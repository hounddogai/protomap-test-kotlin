package com.acme.transport.freightpay.ratecard.audit

import com.acme.transport.freightpay.audit.batch.AuditRule
import com.acme.transport.freightpay.audit.batch.classifyManualAuditProposal

fun summarizeAuditRulesForTemplate(items: MutableList<AuditRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveFallbackRateCardTemplatesFromRule(items: MutableList<AuditRule>): MutableList<FallbackRateCardTemplate> {
    val out = mutableListOf<FallbackRateCardTemplate>()
    for (item in items) {
        val derived = newFallbackRateCardTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyFallbackRateCardTemplate(item: FallbackRateCardTemplate): String {
    return classifyManualAuditProposal(item.effectiveLoad())
}
