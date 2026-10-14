package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.linehaul.laneplan.preference.LegacyLanePlanTarget
import com.acme.transport.linehaul.laneplan.preference.fallbackLanePlanPreferenceWeight

fun summarizeLegacyLanePlanTargetsForRule(items: MutableList<LegacyLanePlanTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLegacyBackhaulRulesFromTarget(items: MutableList<LegacyLanePlanTarget>): MutableList<LegacyBackhaulRule> {
    val out = mutableListOf<LegacyBackhaulRule>()
    for (item in items) {
        val derived = newLegacyBackhaulRule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeLegacyBackhaulRule(item: LegacyBackhaulRule): Double = fallbackLanePlanPreferenceWeight(5, true)
