package com.acme.transport.network.cutoff.summary

import com.acme.transport.network.hub.allocation.CappedHubMapping
import com.acme.transport.network.hub.allocation.rollingHubBaselineWeight

fun summarizeCappedHubMappingsForRequest(items: MutableList<CappedHubMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveCutoffRequestsFromMapping(items: MutableList<CappedHubMapping>): MutableList<CutoffRequest> {
    val out = mutableListOf<CutoffRequest>()
    for (item in items) {
        val derived = newCutoffRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCutoffRequest(item: CutoffRequest): Double = rollingHubBaselineWeight(4, true)
