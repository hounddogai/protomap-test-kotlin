package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.dispute.template.DefaultDisputeTemplate
import com.acme.transport.freightpay.dispute.template.classifyDisputeRollup

fun summarizeDefaultDisputeTemplatesForEntry(items: MutableList<DefaultDisputeTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveFallbackRateCardEntriesFromTemplate(items: MutableList<DefaultDisputeTemplate>): MutableList<FallbackRateCardEntry> {
    val out = mutableListOf<FallbackRateCardEntry>()
    for (item in items) {
        val derived = newFallbackRateCardEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectFallbackRateCardEntry(item: FallbackRateCardEntry): String {
    return classifyDisputeRollup(item.effectiveTotal())
}
