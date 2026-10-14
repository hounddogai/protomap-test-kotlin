package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.emissions.idling.quota.IdlingEstimate
import com.acme.transport.emissions.idling.quota.idlingEstimateBucket

fun summarizeIdlingEstimatesForAgreement(items: MutableList<IdlingEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveTieredEvChargeAgreementsFromEstimate(items: MutableList<IdlingEstimate>): MutableList<TieredEvChargeAgreement> {
    val out = mutableListOf<TieredEvChargeAgreement>()
    for (item in items) {
        val derived = newTieredEvChargeAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileTieredEvChargeAgreement(item: TieredEvChargeAgreement): String = idlingEstimateBucket(item.netLoad())
