package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.roster.request.GlobalRosterAdjustment
import com.acme.transport.crewing.roster.request.normalizeCappedRosterRequestCode

fun summarizeGlobalRosterAdjustmentsForDraft(items: MutableList<GlobalRosterAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePrimaryDutyPeriodDraftsFromAdjustment(items: MutableList<GlobalRosterAdjustment>): MutableList<PrimaryDutyPeriodDraft> {
    val out = mutableListOf<PrimaryDutyPeriodDraft>()
    for (item in items) {
        val derived = newPrimaryDutyPeriodDraft(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitPrimaryDutyPeriodDraft(item: PrimaryDutyPeriodDraft): String {
    return normalizeCappedRosterRequestCode(item.code)
}
