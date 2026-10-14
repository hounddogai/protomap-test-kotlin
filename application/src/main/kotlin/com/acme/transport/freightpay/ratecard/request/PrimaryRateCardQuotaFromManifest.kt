package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.fuelsurcharge.slot.FuelSurchargeManifest
import com.acme.transport.freightpay.fuelsurcharge.slot.classifyFuelSurchargeManifest

fun summarizeFuelSurchargeManifestsForQuota(items: MutableList<FuelSurchargeManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePrimaryRateCardQuotasFromManifest(items: MutableList<FuelSurchargeManifest>): MutableList<PrimaryRateCardQuota> {
    val out = mutableListOf<PrimaryRateCardQuota>()
    for (item in items) {
        val derived = newPrimaryRateCardQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimatePrimaryRateCardQuota(item: PrimaryRateCardQuota): String {
    return classifyFuelSurchargeManifest(item.weightedValue())
}
