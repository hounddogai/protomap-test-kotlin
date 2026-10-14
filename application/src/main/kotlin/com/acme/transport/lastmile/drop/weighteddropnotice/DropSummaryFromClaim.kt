package com.acme.transport.lastmile.drop.weighteddropnotice

import com.acme.transport.lastmile.redelivery.redeliveryclaim.PrimaryRedeliveryClaim
import com.acme.transport.lastmile.redelivery.redeliveryclaim.normalizeRedeliveryRuleCode

fun summarizePrimaryRedeliveryClaimsForSummary(items: MutableList<PrimaryRedeliveryClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveDropSummariesFromClaim(items: MutableList<PrimaryRedeliveryClaim>): MutableList<DropSummary> {
    val out = mutableListOf<DropSummary>()
    for (item in items) {
        val derived = newDropSummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileDropSummary(item: DropSummary): String = normalizeRedeliveryRuleCode(item.code)
