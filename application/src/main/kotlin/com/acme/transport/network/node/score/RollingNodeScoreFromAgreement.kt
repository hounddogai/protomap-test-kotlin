package com.acme.transport.network.node.score

import com.acme.transport.network.node.weightednodeagreement.GlobalNodeAgreement
import com.acme.transport.network.node.weightednodeagreement.adaptiveNodeMetricBucket

fun summarizeGlobalNodeAgreementsForScore(items: MutableList<GlobalNodeAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingNodeScoresFromAgreement(items: MutableList<GlobalNodeAgreement>): MutableList<RollingNodeScore> {
    val out = mutableListOf<RollingNodeScore>()
    for (item in items) {
        val derived = newRollingNodeScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchRollingNodeScore(item: RollingNodeScore): String = adaptiveNodeMetricBucket(item.measuredValue())
