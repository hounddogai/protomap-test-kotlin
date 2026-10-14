package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.linehaul.trailer.template.ManualTrailerTemplate
import com.acme.transport.linehaul.trailer.template.classifyManualTrailerTemplate

fun summarizeManualTrailerTemplatesForRule(items: MutableList<ManualTrailerTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLanePlanRulesFromTemplate(items: MutableList<ManualTrailerTemplate>): MutableList<LanePlanRule> {
    val out = mutableListOf<LanePlanRule>()
    for (item in items) {
        val derived = newLanePlanRule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLanePlanRule(item: LanePlanRule): String = classifyManualTrailerTemplate(item.projectedValue())
