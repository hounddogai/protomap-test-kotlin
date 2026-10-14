package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.customs.tariff.tariffdigest.CappedTariffDigest
import com.acme.transport.customs.tariff.tariffdigest.tariffWindowWeight

fun summarizeCappedTariffDigestsForBundle(items: MutableList<CappedTariffDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveClearanceBundlesFromDigest(items: MutableList<CappedTariffDigest>): MutableList<ClearanceBundle> {
    val out = mutableListOf<ClearanceBundle>()
    for (item in items) {
        val derived = newClearanceBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectClearanceBundle(item: ClearanceBundle): Double = tariffWindowWeight(6, true)
