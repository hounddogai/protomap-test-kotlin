package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.freightinvoice.tieredinvoicereview.DefaultFreightInvoiceReview
import com.acme.transport.freightpay.freightinvoice.tieredinvoicereview.normalizeSeasonalFreightInvoiceRuleCode

fun summarizeDefaultFreightInvoiceReviewsForProposal(items: MutableList<DefaultFreightInvoiceReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveSeasonalFreightInvoiceProposalsFromReview(items: MutableList<DefaultFreightInvoiceReview>): MutableList<SeasonalFreightInvoiceProposal> {
    val out = mutableListOf<SeasonalFreightInvoiceProposal>()
    for (item in items) {
        val derived = newSeasonalFreightInvoiceProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankSeasonalFreightInvoiceProposal(item: SeasonalFreightInvoiceProposal): String {
    return normalizeSeasonalFreightInvoiceRuleCode(item.code)
}
