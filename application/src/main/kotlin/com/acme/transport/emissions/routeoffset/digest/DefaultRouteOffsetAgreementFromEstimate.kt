package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.emissions.idling.estimate.PrimaryIdlingEstimate
import com.acme.transport.emissions.idling.estimate.classifyIdlingReview

fun summarizePrimaryIdlingEstimatesForAgreement(items: MutableList<PrimaryIdlingEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultRouteOffsetAgreementsFromEstimate(items: MutableList<PrimaryIdlingEstimate>): MutableList<DefaultRouteOffsetAgreement> {
    val out = mutableListOf<DefaultRouteOffsetAgreement>()
    for (item in items) {
        val derived = newDefaultRouteOffsetAgreement(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitDefaultRouteOffsetAgreement(item: DefaultRouteOffsetAgreement): String {
    return classifyIdlingReview(item.netTotal())
}
