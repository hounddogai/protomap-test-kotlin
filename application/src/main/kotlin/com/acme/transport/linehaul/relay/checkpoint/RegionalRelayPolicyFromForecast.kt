package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.linehaul.tender.forecast.PendingTenderForecast
import com.acme.transport.linehaul.tender.forecast.normalizeTenderProposalCode

fun summarizePendingTenderForecastsForPolicy(items: MutableList<PendingTenderForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRegionalRelayPoliciesFromForecast(items: MutableList<PendingTenderForecast>): MutableList<RegionalRelayPolicy> {
    val out = mutableListOf<RegionalRelayPolicy>()
    for (item in items) {
        val derived = newRegionalRelayPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRegionalRelayPolicyExtra(item: RegionalRelayPolicy): String = normalizeTenderProposalCode(item.code)
