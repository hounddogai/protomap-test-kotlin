package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.emissions.routeoffset.ledger.DefaultRouteOffsetLedger
import com.acme.transport.emissions.routeoffset.ledger.normalizeRouteOffsetRecordCode

fun summarizeDefaultRouteOffsetLedgersForCycle(items: MutableList<DefaultRouteOffsetLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveBatchedFuelBurnCyclesFromLedger(items: MutableList<DefaultRouteOffsetLedger>): MutableList<BatchedFuelBurnCycle> {
    val out = mutableListOf<BatchedFuelBurnCycle>()
    for (item in items) {
        val derived = newBatchedFuelBurnCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBatchedFuelBurnCycle(item: BatchedFuelBurnCycle): String = normalizeRouteOffsetRecordCode(item.code)
