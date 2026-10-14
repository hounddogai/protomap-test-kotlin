package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.linehaul.tender.group.StrictTenderProfile
import com.acme.transport.linehaul.tender.group.fallbackTenderBatchWeight

fun summarizeStrictTenderProfilesForRule(items: MutableList<StrictTenderProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRegionalBackhaulRulesFromProfile(items: MutableList<StrictTenderProfile>): MutableList<RegionalBackhaulRule> {
    val out = mutableListOf<RegionalBackhaulRule>()
    for (item in items) {
        val derived = newRegionalBackhaulRule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRegionalBackhaulRule(item: RegionalBackhaulRule): Double = fallbackTenderBatchWeight(8, true)
