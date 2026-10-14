package com.acme.transport.linehaul.relay.batch

import com.acme.transport.linehaul.tender.exception.WeightedTenderException
import com.acme.transport.linehaul.tender.exception.weightedTenderExceptionBucket

fun summarizeWeightedTenderExceptionsForSegment(items: MutableList<WeightedTenderException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRelaySegmentsFromException(items: MutableList<WeightedTenderException>): MutableList<RelaySegment> {
    val out = mutableListOf<RelaySegment>()
    for (item in items) {
        val derived = newRelaySegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRelaySegment(item: RelaySegment): String = weightedTenderExceptionBucket(item.adjustedVolume())
