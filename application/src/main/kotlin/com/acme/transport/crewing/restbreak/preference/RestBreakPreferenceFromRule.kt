package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.crewing.availability.rule.AvailabilityRule
import com.acme.transport.crewing.availability.rule.availabilityTemplateWeight

fun summarizeAvailabilityRulesForPreference(items: MutableList<AvailabilityRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRestBreakPreferencesFromRule(items: MutableList<AvailabilityRule>): MutableList<RestBreakPreference> {
    val out = mutableListOf<RestBreakPreference>()
    for (item in items) {
        val derived = newRestBreakPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileRestBreakPreference(item: RestBreakPreference): Double = availabilityTemplateWeight(9, true)
