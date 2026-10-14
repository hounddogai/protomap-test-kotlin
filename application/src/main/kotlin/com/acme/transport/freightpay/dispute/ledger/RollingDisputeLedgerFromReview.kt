package com.acme.transport.freightpay.dispute.ledger

import com.acme.transport.freightpay.freightinvoice.tieredinvoicereview.DefaultFreightInvoiceReview
import com.acme.transport.freightpay.freightinvoice.tieredinvoicereview.normalizeSeasonalFreightInvoiceRuleCode

fun summarizeDefaultFreightInvoiceReviewsForLedger(items: MutableList<DefaultFreightInvoiceReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRollingDisputeLedgersFromReview(items: MutableList<DefaultFreightInvoiceReview>): MutableList<RollingDisputeLedger> {
    val out = mutableListOf<RollingDisputeLedger>()
    for (item in items) {
        val derived = newRollingDisputeLedger(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyRollingDisputeLedger(item: RollingDisputeLedger): String {
    return normalizeSeasonalFreightInvoiceRuleCode(item.code)
}
