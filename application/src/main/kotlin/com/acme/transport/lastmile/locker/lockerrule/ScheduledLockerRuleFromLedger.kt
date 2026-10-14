package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.lastmile.parcel.template.SeasonalParcelLedger
import com.acme.transport.lastmile.parcel.template.normalizeBatchedParcelTemplateCode

fun summarizeSeasonalParcelLedgersForRule(items: MutableList<SeasonalParcelLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveScheduledLockerRulesFromLedger(items: MutableList<SeasonalParcelLedger>): MutableList<ScheduledLockerRule> {
    val out = mutableListOf<ScheduledLockerRule>()
    for (item in items) {
        val derived = newScheduledLockerRule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveScheduledLockerRule(item: ScheduledLockerRule): String = normalizeBatchedParcelTemplateCode(item.code)
