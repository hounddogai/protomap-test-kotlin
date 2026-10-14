package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.freightpay.dispute.disputetemplate.ManualDisputeSnapshot
import com.acme.transport.freightpay.dispute.disputetemplate.manualDisputeSnapshotWeight

fun summarizeManualDisputeSnapshotsForLedger(items: MutableList<ManualDisputeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveFallbackAccessorialLedgersFromSnapshot(items: MutableList<ManualDisputeSnapshot>): MutableList<FallbackAccessorialLedger> {
    val out = mutableListOf<FallbackAccessorialLedger>()
    for (item in items) {
        val derived = newFallbackAccessorialLedger(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateFallbackAccessorialLedger(item: FallbackAccessorialLedger): Double {
    return manualDisputeSnapshotWeight(4, true)
}
