package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.freightpay.dispute.adaptivedisputeforecast.StrictDisputeForecast
import com.acme.transport.freightpay.dispute.adaptivedisputeforecast.normalizeTieredDisputePreferenceCode

fun summarizeStrictDisputeForecastsForManifest(items: MutableList<StrictDisputeForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLenientRateCardManifestsFromForecast(items: MutableList<StrictDisputeForecast>): MutableList<LenientRateCardManifest> {
    val out = mutableListOf<LenientRateCardManifest>()
    for (item in items) {
        val derived = newLenientRateCardManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planLenientRateCardManifest(item: LenientRateCardManifest): String {
    return normalizeTieredDisputePreferenceCode(item.code)
}
