package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.linehaul.capacity.milestone.CapacityRule
import com.acme.transport.linehaul.capacity.milestone.batchedCapacityGroupBucket

fun summarizeCapacityRulesForAgreement(items: MutableList<CapacityRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveCappedTrailerAgreementsFromRule(items: MutableList<CapacityRule>): MutableList<CappedTrailerAgreement> {
    val out = mutableListOf<CappedTrailerAgreement>()
    for (item in items) {
        val derived = newCappedTrailerAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedTrailerAgreement(item: CappedTrailerAgreement): String {
    return batchedCapacityGroupBucket(item.adjustedExposure())
}
