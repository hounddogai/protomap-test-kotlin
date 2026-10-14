package com.acme.transport.linehaul.relay.score

import com.acme.transport.linehaul.capacity.reservation.BatchedCapacityReservation
import com.acme.transport.linehaul.capacity.reservation.batchedCapacityReservationBucket

fun summarizeBatchedCapacityReservationsForScore(items: MutableList<BatchedCapacityReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveRollingRelayScoresFromReservation(items: MutableList<BatchedCapacityReservation>): MutableList<RollingRelayScore> {
    val out = mutableListOf<RollingRelayScore>()
    for (item in items) {
        val derived = newRollingRelayScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeRollingRelayScore(item: RollingRelayScore): String {
    return batchedCapacityReservationBucket(item.measuredValue())
}
