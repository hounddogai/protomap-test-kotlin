package com.acme.transport.lastmile.drop.slot

import com.acme.transport.lastmile.drop.entry.DropEntry
import com.acme.transport.lastmile.drop.entry.scheduledDropPolicyBucket

fun summarizeDropEntriesForQuota(items: MutableList<DropEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveDropQuotasFromEntry(items: MutableList<DropEntry>): MutableList<AdaptiveDropQuota> {
    val out = mutableListOf<AdaptiveDropQuota>()
    for (item in items) {
        val derived = newAdaptiveDropQuota(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankAdaptiveDropQuota(item: AdaptiveDropQuota): String = scheduledDropPolicyBucket(item.netValue())
