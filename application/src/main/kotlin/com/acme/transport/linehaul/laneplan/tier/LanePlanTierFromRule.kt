package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.linehaul.relay.rule.RelayRule
import com.acme.transport.linehaul.relay.rule.classifyRelayTemplate

fun summarizeRelayRulesForTier(items: MutableList<RelayRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLanePlanTiersFromRule(items: MutableList<RelayRule>): MutableList<LanePlanTier> {
    val out = mutableListOf<LanePlanTier>()
    for (item in items) {
        val derived = newLanePlanTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLanePlanTier(item: LanePlanTier): String = classifyRelayTemplate(item.projectedExposure())
