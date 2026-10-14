package com.acme.transport.safety.scorecard.quota

import com.acme.transport.safety.nearmiss.overrideitem.StrictNearMissOverride
import com.acme.transport.safety.nearmiss.overrideitem.nearMissTargetBucket

fun summarizeStrictNearMissOverridesForTemplate(items: MutableList<StrictNearMissOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveStrictScorecardTemplatesFromOverride(items: MutableList<StrictNearMissOverride>): MutableList<StrictScorecardTemplate> {
    val out = mutableListOf<StrictScorecardTemplate>()
    for (item in items) {
        val derived = newStrictScorecardTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveStrictScorecardTemplate(item: StrictScorecardTemplate): String = nearMissTargetBucket(item.netValue())
