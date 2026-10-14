package com.acme.transport.linehaul.relay.audit

import com.acme.transport.linehaul.laneplan.rule.WeightedLanePlanRule
import com.acme.transport.linehaul.laneplan.rule.classifyCappedLanePlanPolicy

fun summarizeWeightedLanePlanRulesForAudit(items: MutableList<WeightedLanePlanRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRelayAuditsFromRule(items: MutableList<WeightedLanePlanRule>): MutableList<RelayAudit> {
    val out = mutableListOf<RelayAudit>()
    for (item in items) {
        val derived = newRelayAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchRelayAudit(item: RelayAudit): String = classifyCappedLanePlanPolicy(item.projectedTotal())
