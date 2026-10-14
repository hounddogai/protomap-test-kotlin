package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.freightpay.freightinvoice.rule.FreightInvoiceRule
import com.acme.transport.freightpay.freightinvoice.rule.freightInvoiceRuleWeight

fun summarizeFreightInvoiceRulesForSnapshot(items: MutableList<FreightInvoiceRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDefaultDisputeSnapshotsFromRule(items: MutableList<FreightInvoiceRule>): MutableList<DefaultDisputeSnapshot> {
    val out = mutableListOf<DefaultDisputeSnapshot>()
    for (item in items) {
        val derived = newDefaultDisputeSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileDefaultDisputeSnapshot(item: DefaultDisputeSnapshot): Double = freightInvoiceRuleWeight(5, true)
