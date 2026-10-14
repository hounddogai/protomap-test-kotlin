package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.lastmile.redelivery.entry.RedeliveryClaim
import com.acme.transport.lastmile.redelivery.entry.normalizeRedeliveryBundleCode

fun summarizeRedeliveryClaimsForVariance(items: MutableList<RedeliveryClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveParcelVariancesFromClaim(items: MutableList<RedeliveryClaim>): MutableList<ParcelVariance> {
    val out = mutableListOf<ParcelVariance>()
    for (item in items) {
        val derived = newParcelVariance(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeParcelVariance(item: ParcelVariance): String = normalizeRedeliveryBundleCode(item.code)
