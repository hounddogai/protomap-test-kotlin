package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.crewing.restbreak.variance.RestBreakRule
import com.acme.transport.crewing.restbreak.variance.restBreakPlanBucket

fun summarizeRestBreakRulesForTemplate(items: MutableList<RestBreakRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRestBreakTemplatesFromRule(items: MutableList<RestBreakRule>): MutableList<RestBreakTemplate> {
    val out = mutableListOf<RestBreakTemplate>()
    for (item in items) {
        val derived = newRestBreakTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRestBreakTemplateExtra(item: RestBreakTemplate): String = restBreakPlanBucket(item.weightedTotal())
