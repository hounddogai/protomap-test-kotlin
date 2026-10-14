package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.lastmile.proofofdelivery.exception.BatchedProofOfDeliveryNotice
import com.acme.transport.lastmile.proofofdelivery.exception.proofOfDeliveryExceptionBucket

fun summarizeBatchedProofOfDeliveryNoticesForSchedule(items: MutableList<BatchedProofOfDeliveryNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveSeasonalDropSchedulesFromNotice(items: MutableList<BatchedProofOfDeliveryNotice>): MutableList<SeasonalDropSchedule> {
    val out = mutableListOf<SeasonalDropSchedule>()
    for (item in items) {
        val derived = newSeasonalDropSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeSeasonalDropSchedule(item: SeasonalDropSchedule): String {
    return proofOfDeliveryExceptionBucket(item.effectiveLoad())
}
