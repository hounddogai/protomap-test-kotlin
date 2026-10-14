package com.acme.transport.network.hub.agreement

import com.acme.transport.network.servicearea.estimate.ServiceAreaEstimate
import com.acme.transport.network.servicearea.estimate.classifyServiceAreaSlot

fun summarizeServiceAreaEstimatesForAgreement(items: MutableList<ServiceAreaEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveHubAgreementsFromEstimate(items: MutableList<ServiceAreaEstimate>): MutableList<HubAgreement> {
    val out = mutableListOf<HubAgreement>()
    for (item in items) {
        val derived = newHubAgreement(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchHubAgreement(item: HubAgreement): String = classifyServiceAreaSlot(item.adjustedValue())
