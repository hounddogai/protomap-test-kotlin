package com.acme.transport.network.transittime.timedigest

import com.acme.transport.network.servicearea.agreement.RegionalServiceAreaAgreement
import com.acme.transport.network.servicearea.agreement.classifyServiceAreaMapping

fun summarizeRegionalServiceAreaAgreementsForDigest(items: MutableList<RegionalServiceAreaAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedTransitTimeDigestsFromAgreement(items: MutableList<RegionalServiceAreaAgreement>): MutableList<WeightedTransitTimeDigest> {
    val out = mutableListOf<WeightedTransitTimeDigest>()
    for (item in items) {
        val derived = newWeightedTransitTimeDigest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceWeightedTransitTimeDigest(item: WeightedTransitTimeDigest): String {
    return classifyServiceAreaMapping(item.effectiveValue())
}
