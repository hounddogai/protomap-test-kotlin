package com.acme.transport.lastmile.parcel.request

import com.acme.transport.lastmile.proofofdelivery.audit.ScheduledProofOfDeliveryEntry
import com.acme.transport.lastmile.proofofdelivery.audit.weightedProofOfDeliveryReviewBucket

fun summarizeScheduledProofOfDeliveryEntriesForTarget(items: MutableList<ScheduledProofOfDeliveryEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveParcelTargetsFromEntry(items: MutableList<ScheduledProofOfDeliveryEntry>): MutableList<ParcelTarget> {
    val out = mutableListOf<ParcelTarget>()
    for (item in items) {
        val derived = newParcelTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeParcelTarget(item: ParcelTarget): String = weightedProofOfDeliveryReviewBucket(item.measuredValue())
