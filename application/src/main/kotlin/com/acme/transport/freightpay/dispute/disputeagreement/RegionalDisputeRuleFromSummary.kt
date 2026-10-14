package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.freightpay.freightinvoice.adjustment.WeightedFreightInvoiceSummary
import com.acme.transport.freightpay.freightinvoice.adjustment.normalizeWeightedFreightInvoiceSummaryCode

fun summarizeWeightedFreightInvoiceSummariesForRule(items: MutableList<WeightedFreightInvoiceSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRegionalDisputeRulesFromSummary(items: MutableList<WeightedFreightInvoiceSummary>): MutableList<RegionalDisputeRule> {
    val out = mutableListOf<RegionalDisputeRule>()
    for (item in items) {
        val derived = newRegionalDisputeRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRegionalDisputeRule(item: RegionalDisputeRule): String {
    return normalizeWeightedFreightInvoiceSummaryCode(item.code)
}
