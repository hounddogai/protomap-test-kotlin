package com.acme.transport.crewing.availability.rule

import com.acme.transport.crewing.restbreak.snapshot.RestBreakAudit
import com.acme.transport.crewing.restbreak.snapshot.restBreakAuditWeight

fun summarizeRestBreakAuditsForRule(items: MutableList<RestBreakAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveAvailabilityRulesFromAudit(items: MutableList<RestBreakAudit>): MutableList<AvailabilityRule> {
    val out = mutableListOf<AvailabilityRule>()
    for (item in items) {
        val derived = newAvailabilityRule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileAvailabilityRule(item: AvailabilityRule): Double = restBreakAuditWeight(3, true)
