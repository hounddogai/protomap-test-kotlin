package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.lastmile.attempt.quota.AttemptQuota
import com.acme.transport.lastmile.attempt.quota.classifyAttemptQuota

fun summarizeAttemptQuotasForBundle(items: MutableList<AttemptQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveSeasonalAttemptBundlesFromQuota(items: MutableList<AttemptQuota>): MutableList<SeasonalAttemptBundle> {
    val out = mutableListOf<SeasonalAttemptBundle>()
    for (item in items) {
        val derived = newSeasonalAttemptBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreSeasonalAttemptBundle(item: SeasonalAttemptBundle): String = classifyAttemptQuota(item.netExposure())
