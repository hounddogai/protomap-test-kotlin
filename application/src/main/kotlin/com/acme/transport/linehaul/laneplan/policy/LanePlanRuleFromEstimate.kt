package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.linehaul.laneplan.segment.LenientLanePlanEstimate
import com.acme.transport.linehaul.laneplan.segment.classifyLenientLanePlanEstimate

fun summarizeLenientLanePlanEstimatesForRule(items: MutableList<LenientLanePlanEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLanePlanRulesFromEstimate(items: MutableList<LenientLanePlanEstimate>): MutableList<LanePlanRule> {
    val out = mutableListOf<LanePlanRule>()
    for (item in items) {
        val derived = newLanePlanRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveLanePlanRule(item: LanePlanRule): String = classifyLenientLanePlanEstimate(item.projectedValue())
