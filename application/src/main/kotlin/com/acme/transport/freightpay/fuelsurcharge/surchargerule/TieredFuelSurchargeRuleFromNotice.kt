package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.freightpay.freightinvoice.proposal.LegacyFreightInvoiceNotice
import com.acme.transport.freightpay.freightinvoice.proposal.freightInvoiceProposalBucket

fun summarizeLegacyFreightInvoiceNoticesForRule(items: MutableList<LegacyFreightInvoiceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveTieredFuelSurchargeRulesFromNotice(items: MutableList<LegacyFreightInvoiceNotice>): MutableList<TieredFuelSurchargeRule> {
    val out = mutableListOf<TieredFuelSurchargeRule>()
    for (item in items) {
        val derived = newTieredFuelSurchargeRule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyTieredFuelSurchargeRuleExtra(item: TieredFuelSurchargeRule): String {
    return freightInvoiceProposalBucket(item.weightedValue())
}
