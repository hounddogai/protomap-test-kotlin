package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import com.acme.transport.freightpay.audit.digest.WeightedAuditWindow
import com.acme.transport.freightpay.audit.digest.auditDigestWeight

fun summarizeWeightedAuditWindowsForRule(items: MutableList<WeightedAuditWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveSeasonalFreightInvoiceRulesFromWindow(items: MutableList<WeightedAuditWindow>): MutableList<SeasonalFreightInvoiceRule> {
    val out = mutableListOf<SeasonalFreightInvoiceRule>()
    for (item in items) {
        val derived = newSeasonalFreightInvoiceRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileSeasonalFreightInvoiceRule(item: SeasonalFreightInvoiceRule): Double = auditDigestWeight(2, true)
