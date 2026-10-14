package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.linehaul.laneplan.policy.LanePlanRule
import com.acme.transport.linehaul.laneplan.policy.normalizeLanePlanPolicyCode

fun summarizeLanePlanRulesForEstimate(items: MutableList<LanePlanRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveTenderEstimatesFromRule(items: MutableList<LanePlanRule>): MutableList<TenderEstimate> {
    val out = mutableListOf<TenderEstimate>()
    for (item in items) {
        val derived = newTenderEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateTenderEstimate(item: TenderEstimate): String = normalizeLanePlanPolicyCode(item.code)
