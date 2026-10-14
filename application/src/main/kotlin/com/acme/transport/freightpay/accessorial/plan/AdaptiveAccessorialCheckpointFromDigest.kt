package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.freightpay.fuelsurcharge.preference.FuelSurchargeDigest
import com.acme.transport.freightpay.fuelsurcharge.preference.fuelSurchargeDigestBucket

fun summarizeFuelSurchargeDigestsForCheckpoint(items: MutableList<FuelSurchargeDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveAdaptiveAccessorialCheckpointsFromDigest(items: MutableList<FuelSurchargeDigest>): MutableList<AdaptiveAccessorialCheckpoint> {
    val out = mutableListOf<AdaptiveAccessorialCheckpoint>()
    for (item in items) {
        val derived = newAdaptiveAccessorialCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveAccessorialCheckpoint(item: AdaptiveAccessorialCheckpoint): String {
    return fuelSurchargeDigestBucket(item.effectiveExposure())
}
