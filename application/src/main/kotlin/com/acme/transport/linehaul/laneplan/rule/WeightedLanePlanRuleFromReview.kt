package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.linehaul.laneplan.item.FallbackLanePlanReview
import com.acme.transport.linehaul.laneplan.item.normalizeCappedLanePlanPreferenceCode

fun summarizeFallbackLanePlanReviewsForRule(items: MutableList<FallbackLanePlanReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveWeightedLanePlanRulesFromReview(items: MutableList<FallbackLanePlanReview>): MutableList<WeightedLanePlanRule> {
    val out = mutableListOf<WeightedLanePlanRule>()
    for (item in items) {
        val derived = newWeightedLanePlanRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreWeightedLanePlanRule(item: WeightedLanePlanRule): String {
    return normalizeCappedLanePlanPreferenceCode(item.code)
}
