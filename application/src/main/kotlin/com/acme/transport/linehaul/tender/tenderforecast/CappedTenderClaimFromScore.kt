package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.linehaul.backhaul.cappedbackhaulvariance.PendingBackhaulScore
import com.acme.transport.linehaul.backhaul.cappedbackhaulvariance.lenientBackhaulVarianceWeight

fun summarizePendingBackhaulScoresForClaim(items: MutableList<PendingBackhaulScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCappedTenderClaimsFromScore(items: MutableList<PendingBackhaulScore>): MutableList<CappedTenderClaim> {
    val out = mutableListOf<CappedTenderClaim>()
    for (item in items) {
        val derived = newCappedTenderClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveCappedTenderClaim(item: CappedTenderClaim): Double = lenientBackhaulVarianceWeight(3, true)
