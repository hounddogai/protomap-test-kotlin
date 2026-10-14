package com.acme.transport.linehaul.tender.batch

import com.acme.transport.linehaul.backhaul.variance.BackhaulVariance
import com.acme.transport.linehaul.backhaul.variance.backhaulManifestBucket

fun summarizeBackhaulVariancesForClaim(items: MutableList<BackhaulVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveTenderClaimsFromVariance(items: MutableList<BackhaulVariance>): MutableList<TenderClaim> {
    val out = mutableListOf<TenderClaim>()
    for (item in items) {
        val derived = newTenderClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planTenderClaim(item: TenderClaim): String = backhaulManifestBucket(item.weightedExposure())
