package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.safety.nearmiss.missrule.PrimaryNearMissRule
import com.acme.transport.safety.nearmiss.missrule.normalizePrimaryNearMissRuleCode

fun summarizePrimaryNearMissRulesForMapping(items: MutableList<PrimaryNearMissRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveNearMissMappingsFromRule(items: MutableList<PrimaryNearMissRule>): MutableList<NearMissMapping> {
    val out = mutableListOf<NearMissMapping>()
    for (item in items) {
        val derived = newNearMissMapping(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignNearMissMapping(item: NearMissMapping): String = normalizePrimaryNearMissRuleCode(item.code)
