package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.fuelsurcharge.window.SeasonalFuelSurchargeClaim
import com.acme.transport.freightpay.fuelsurcharge.window.normalizeFuelSurchargeMilestoneCode

fun summarizeSeasonalFuelSurchargeClaimsForManifest(items: MutableList<SeasonalFuelSurchargeClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRateCardManifestsFromClaim(items: MutableList<SeasonalFuelSurchargeClaim>): MutableList<RateCardManifest> {
    val out = mutableListOf<RateCardManifest>()
    for (item in items) {
        val derived = newRateCardManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateRateCardManifest(item: RateCardManifest): String = normalizeFuelSurchargeMilestoneCode(item.code)
