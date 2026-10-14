package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.dispute.checkpoint.StrictDisputeQuota
import com.acme.transport.freightpay.dispute.checkpoint.strictDisputeQuotaWeight

fun summarizeStrictDisputeQuotasForTemplate(items: MutableList<StrictDisputeQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDefaultDisputeTemplatesFromQuota(items: MutableList<StrictDisputeQuota>): MutableList<DefaultDisputeTemplate> {
    val out = mutableListOf<DefaultDisputeTemplate>()
    for (item in items) {
        val derived = newDefaultDisputeTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileDefaultDisputeTemplate(item: DefaultDisputeTemplate): Double = strictDisputeQuotaWeight(4, true)
