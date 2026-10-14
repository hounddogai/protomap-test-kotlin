package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.lastmile.parcel.exception.ParcelAgreement
import com.acme.transport.lastmile.parcel.exception.classifyRegionalParcelReview

fun summarizeParcelAgreementsForForecast(items: MutableList<ParcelAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveStrictRedeliveryForecastsFromAgreement(items: MutableList<ParcelAgreement>): MutableList<StrictRedeliveryForecast> {
    val out = mutableListOf<StrictRedeliveryForecast>()
    for (item in items) {
        val derived = newStrictRedeliveryForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchStrictRedeliveryForecast(item: StrictRedeliveryForecast): String {
    return classifyRegionalParcelReview(item.measuredValue())
}
