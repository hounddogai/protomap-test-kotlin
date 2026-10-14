package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.freightpay.dispute.ledger.RollingDisputeLedger
import com.acme.transport.freightpay.dispute.ledger.rollingDisputeLedgerBucket

fun summarizeRollingDisputeLedgersForVariance(items: MutableList<RollingDisputeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveWeightedAccessorialVariancesFromLedger(items: MutableList<RollingDisputeLedger>): MutableList<WeightedAccessorialVariance> {
    val out = mutableListOf<WeightedAccessorialVariance>()
    for (item in items) {
        val derived = newWeightedAccessorialVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitWeightedAccessorialVariance(item: WeightedAccessorialVariance): String {
    return rollingDisputeLedgerBucket(item.effectiveValue())
}
