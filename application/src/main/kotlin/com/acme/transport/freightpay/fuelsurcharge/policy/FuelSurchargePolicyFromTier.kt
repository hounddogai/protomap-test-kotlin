package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.freightpay.dispute.manifest.LenientDisputeTier
import com.acme.transport.freightpay.dispute.manifest.defaultDisputeManifestWeight

fun summarizeLenientDisputeTiersForPolicy(items: MutableList<LenientDisputeTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFuelSurchargePoliciesFromTier(items: MutableList<LenientDisputeTier>): MutableList<FuelSurchargePolicy> {
    val out = mutableListOf<FuelSurchargePolicy>()
    for (item in items) {
        val derived = newFuelSurchargePolicy(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeFuelSurchargePolicy(item: FuelSurchargePolicy): Double = defaultDisputeManifestWeight(4, true)
