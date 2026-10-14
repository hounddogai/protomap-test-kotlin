package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.lastmile.drop.slot.AdaptiveDropQuota
import com.acme.transport.lastmile.drop.slot.dropSlotBucket

fun summarizeAdaptiveDropQuotasForAdjustment(items: MutableList<AdaptiveDropQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAttemptAdjustmentsFromQuota(items: MutableList<AdaptiveDropQuota>): MutableList<AttemptAdjustment> {
    val out = mutableListOf<AttemptAdjustment>()
    for (item in items) {
        val derived = newAttemptAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreAttemptAdjustment(item: AttemptAdjustment): String = dropSlotBucket(item.adjustedLoad())
