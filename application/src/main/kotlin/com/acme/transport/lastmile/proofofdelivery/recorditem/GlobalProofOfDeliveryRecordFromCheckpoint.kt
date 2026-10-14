package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.lastmile.redelivery.threshold.TieredRedeliveryCheckpoint
import com.acme.transport.lastmile.redelivery.threshold.redeliveryEstimateWeight

fun summarizeTieredRedeliveryCheckpointsForRecord(items: MutableList<TieredRedeliveryCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveGlobalProofOfDeliveryRecordsFromCheckpoint(items: MutableList<TieredRedeliveryCheckpoint>): MutableList<GlobalProofOfDeliveryRecord> {
    val out = mutableListOf<GlobalProofOfDeliveryRecord>()
    for (item in items) {
        val derived = newGlobalProofOfDeliveryRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeGlobalProofOfDeliveryRecord(item: GlobalProofOfDeliveryRecord): Double {
    return redeliveryEstimateWeight(7, true)
}
