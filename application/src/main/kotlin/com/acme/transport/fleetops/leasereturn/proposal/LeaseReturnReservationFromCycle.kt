package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.leasereturn.ledger.LeaseReturnCycle
import com.acme.transport.fleetops.leasereturn.ledger.leaseReturnCycleBucket

fun summarizeLeaseReturnCyclesForReservation(items: MutableList<LeaseReturnCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLeaseReturnReservationsFromCycle(items: MutableList<LeaseReturnCycle>): MutableList<LeaseReturnReservation> {
    val out = mutableListOf<LeaseReturnReservation>()
    for (item in items) {
        val derived = newLeaseReturnReservation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankLeaseReturnReservation(item: LeaseReturnReservation): String {
    return leaseReturnCycleBucket(item.weightedTotal())
}
