package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.terminal.crossdock.dockestimate.RegionalCrossDockEstimate
import com.acme.transport.terminal.crossdock.dockestimate.regionalCrossDockEstimateBucket

fun summarizeRegionalCrossDockEstimatesForAudit(items: MutableList<RegionalCrossDockEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveGlobalTrailerYardAuditsFromEstimate(items: MutableList<RegionalCrossDockEstimate>): MutableList<GlobalTrailerYardAudit> {
    val out = mutableListOf<GlobalTrailerYardAudit>()
    for (item in items) {
        val derived = newGlobalTrailerYardAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateGlobalTrailerYardAudit(item: GlobalTrailerYardAudit): String {
    return regionalCrossDockEstimateBucket(item.netExposure())
}
