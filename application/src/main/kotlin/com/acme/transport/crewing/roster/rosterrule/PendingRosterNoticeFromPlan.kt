package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.roster.budget.ManualRosterPlan
import com.acme.transport.crewing.roster.budget.batchedRosterTemplateBucket

fun summarizeManualRosterPlansForNotice(items: MutableList<ManualRosterPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePendingRosterNoticesFromPlan(items: MutableList<ManualRosterPlan>): MutableList<PendingRosterNotice> {
    val out = mutableListOf<PendingRosterNotice>()
    for (item in items) {
        val derived = newPendingRosterNotice(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun derivePendingRosterNotice(item: PendingRosterNotice): String {
    return batchedRosterTemplateBucket(item.measuredValue())
}
