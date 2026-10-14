package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.linehaul.trailer.batch.TrailerBatch
import com.acme.transport.linehaul.trailer.batch.trailerEntryWeight

fun summarizeTrailerBatchesForRequest(items: MutableList<TrailerBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRelayRequestsFromBatch(items: MutableList<TrailerBatch>): MutableList<RelayRequest> {
    val out = mutableListOf<RelayRequest>()
    for (item in items) {
        val derived = newRelayRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRelayRequest(item: RelayRequest): Double = trailerEntryWeight(1, true)
