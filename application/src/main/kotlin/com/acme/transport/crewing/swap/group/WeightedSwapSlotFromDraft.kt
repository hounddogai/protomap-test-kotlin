package com.acme.transport.crewing.swap.group

import com.acme.transport.crewing.swap.draft.RollingSwapDraft
import com.acme.transport.crewing.swap.draft.scheduledSwapNoticeBucket

fun summarizeRollingSwapDraftsForSlot(items: MutableList<RollingSwapDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedSwapSlotsFromDraft(items: MutableList<RollingSwapDraft>): MutableList<WeightedSwapSlot> {
    val out = mutableListOf<WeightedSwapSlot>()
    for (item in items) {
        val derived = newWeightedSwapSlot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveWeightedSwapSlot(item: WeightedSwapSlot): String = scheduledSwapNoticeBucket(item.adjustedValue())
