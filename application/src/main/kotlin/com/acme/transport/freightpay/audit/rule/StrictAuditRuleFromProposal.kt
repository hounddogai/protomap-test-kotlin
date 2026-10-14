package com.acme.transport.freightpay.audit.rule

import com.acme.transport.freightpay.freightinvoice.proposal.FreightInvoiceProposal
import com.acme.transport.freightpay.freightinvoice.proposal.strictFreightInvoiceRequestWeight

fun summarizeFreightInvoiceProposalsForRule(items: MutableList<FreightInvoiceProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveStrictAuditRulesFromProposal(items: MutableList<FreightInvoiceProposal>): MutableList<StrictAuditRule> {
    val out = mutableListOf<StrictAuditRule>()
    for (item in items) {
        val derived = newStrictAuditRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeStrictAuditRule(item: StrictAuditRule): Double = strictFreightInvoiceRequestWeight(5, true)
