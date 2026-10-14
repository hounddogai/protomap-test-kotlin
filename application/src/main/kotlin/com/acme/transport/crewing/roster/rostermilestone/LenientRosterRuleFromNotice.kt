package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.roster.template.PrimaryRosterNotice
import com.acme.transport.crewing.roster.template.normalizeRosterBaselineCode

fun summarizePrimaryRosterNoticesForRule(items: MutableList<PrimaryRosterNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLenientRosterRulesFromNotice(items: MutableList<PrimaryRosterNotice>): MutableList<LenientRosterRule> {
    val out = mutableListOf<LenientRosterRule>()
    for (item in items) {
        val derived = newLenientRosterRule(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLenientRosterRule(item: LenientRosterRule): String = normalizeRosterBaselineCode(item.code)
