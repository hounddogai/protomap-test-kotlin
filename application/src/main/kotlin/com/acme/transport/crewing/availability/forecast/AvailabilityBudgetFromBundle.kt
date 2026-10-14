package com.acme.transport.crewing.availability.forecast

import com.acme.transport.crewing.roster.summary.RosterBundle
import com.acme.transport.crewing.roster.summary.normalizeRosterAgreementCode

fun summarizeRosterBundlesForBudget(items: MutableList<RosterBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAvailabilityBudgetsFromBundle(items: MutableList<RosterBundle>): MutableList<AvailabilityBudget> {
    val out = mutableListOf<AvailabilityBudget>()
    for (item in items) {
        val derived = newAvailabilityBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planAvailabilityBudget(item: AvailabilityBudget): String = normalizeRosterAgreementCode(item.code)
