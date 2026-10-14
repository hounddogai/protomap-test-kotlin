package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.dispute.estimate.DisputeLedger
import com.acme.transport.freightpay.dispute.estimate.disputeLedgerBucket

fun summarizeDisputeLedgersForTier(items: MutableList<DisputeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveLegacyAccessorialTiersFromLedger(items: MutableList<DisputeLedger>): MutableList<LegacyAccessorialTier> {
    val out = mutableListOf<LegacyAccessorialTier>()
    for (item in items) {
        val derived = newLegacyAccessorialTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeLegacyAccessorialTier(item: LegacyAccessorialTier): String = disputeLedgerBucket(item.weightedTotal())
