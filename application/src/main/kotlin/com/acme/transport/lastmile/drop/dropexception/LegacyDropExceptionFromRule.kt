package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.lastmile.drop.rule.PrimaryDropRule
import com.acme.transport.lastmile.drop.rule.primaryDropRuleBucket

fun summarizePrimaryDropRulesForException(items: MutableList<PrimaryDropRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLegacyDropExceptionsFromRule(items: MutableList<PrimaryDropRule>): MutableList<LegacyDropException> {
    val out = mutableListOf<LegacyDropException>()
    for (item in items) {
        val derived = newLegacyDropException(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectLegacyDropException(item: LegacyDropException): String = primaryDropRuleBucket(item.weightedLoad())
