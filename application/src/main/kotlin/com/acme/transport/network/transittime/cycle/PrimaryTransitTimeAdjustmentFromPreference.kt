package com.acme.transport.network.transittime.cycle

import com.acme.transport.network.transittime.draft.ScheduledTransitTimePreference
import com.acme.transport.network.transittime.draft.normalizeBatchedTransitTimeRuleCode

fun summarizeScheduledTransitTimePreferencesForAdjustment(items: MutableList<ScheduledTransitTimePreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePrimaryTransitTimeAdjustmentsFromPreference(items: MutableList<ScheduledTransitTimePreference>): MutableList<PrimaryTransitTimeAdjustment> {
    val out = mutableListOf<PrimaryTransitTimeAdjustment>()
    for (item in items) {
        val derived = newPrimaryTransitTimeAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcilePrimaryTransitTimeAdjustment(item: PrimaryTransitTimeAdjustment): String {
    return normalizeBatchedTransitTimeRuleCode(item.code)
}
