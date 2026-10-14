package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.fleetops.serviceplan.variance.ServicePlanReview
import com.acme.transport.fleetops.serviceplan.variance.normalizePendingServicePlanLedgerCode

fun summarizeServicePlanReviewsForLedger(items: MutableList<ServicePlanReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveDefaultServicePlanLedgersFromReview(items: MutableList<ServicePlanReview>): MutableList<DefaultServicePlanLedger> {
    val out = mutableListOf<DefaultServicePlanLedger>()
    for (item in items) {
        val derived = newDefaultServicePlanLedger(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDefaultServicePlanLedger(item: DefaultServicePlanLedger): String {
    return normalizePendingServicePlanLedgerCode(item.code)
}
