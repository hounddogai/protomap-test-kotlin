package com.acme.transport.network.cutoff.tier

import com.acme.transport.network.node.seasonalnodeagreement.CappedNodeAgreement
import com.acme.transport.network.node.seasonalnodeagreement.normalizeCappedNodeAgreementCode

fun summarizeCappedNodeAgreementsForRule(items: MutableList<CappedNodeAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveStrictCutoffRulesFromAgreement(items: MutableList<CappedNodeAgreement>): MutableList<StrictCutoffRule> {
    val out = mutableListOf<StrictCutoffRule>()
    for (item in items) {
        val derived = newStrictCutoffRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeStrictCutoffRule(item: StrictCutoffRule): String = normalizeCappedNodeAgreementCode(item.code)
