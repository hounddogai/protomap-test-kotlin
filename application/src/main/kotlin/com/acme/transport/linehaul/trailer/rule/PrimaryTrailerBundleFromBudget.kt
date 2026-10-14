package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.linehaul.laneplan.budget.LanePlanBudget
import com.acme.transport.linehaul.laneplan.budget.lanePlanSummaryBucket

fun summarizeLanePlanBudgetsForBundle(items: MutableList<LanePlanBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePrimaryTrailerBundlesFromBudget(items: MutableList<LanePlanBudget>): MutableList<PrimaryTrailerBundle> {
    val out = mutableListOf<PrimaryTrailerBundle>()
    for (item in items) {
        val derived = newPrimaryTrailerBundle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankPrimaryTrailerBundle(item: PrimaryTrailerBundle): String = lanePlanSummaryBucket(item.netVolume())
