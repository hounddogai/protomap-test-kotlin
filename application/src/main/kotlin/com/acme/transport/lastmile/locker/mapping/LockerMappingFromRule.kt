package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.lastmile.drop.rule.PrimaryDropRule
import com.acme.transport.lastmile.drop.rule.primaryDropRuleWeight

fun summarizePrimaryDropRulesForMapping(items: MutableList<PrimaryDropRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLockerMappingsFromRule(items: MutableList<PrimaryDropRule>): MutableList<LockerMapping> {
    val out = mutableListOf<LockerMapping>()
    for (item in items) {
        val derived = newLockerMapping(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileLockerMapping(item: LockerMapping): Double = primaryDropRuleWeight(7, true)
