package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.lastmile.drop.rule.TieredDropAdjustment
import com.acme.transport.lastmile.drop.rule.primaryDropRuleWeight

fun summarizeTieredDropAdjustmentsForSlot(items: MutableList<TieredDropAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveScheduledAttemptSlotsFromAdjustment(items: MutableList<TieredDropAdjustment>): MutableList<ScheduledAttemptSlot> {
    val out = mutableListOf<ScheduledAttemptSlot>()
    for (item in items) {
        val derived = newScheduledAttemptSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateScheduledAttemptSlot(item: ScheduledAttemptSlot): Double = primaryDropRuleWeight(1, true)
