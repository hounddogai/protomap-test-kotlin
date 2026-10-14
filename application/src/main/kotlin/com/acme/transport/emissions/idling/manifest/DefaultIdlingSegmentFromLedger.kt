package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.fuelburn.ledger.FuelBurnLedger
import com.acme.transport.emissions.fuelburn.ledger.normalizeWeightedFuelBurnMappingCode

fun summarizeFuelBurnLedgersForSegment(items: MutableList<FuelBurnLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDefaultIdlingSegmentsFromLedger(items: MutableList<FuelBurnLedger>): MutableList<DefaultIdlingSegment> {
    val out = mutableListOf<DefaultIdlingSegment>()
    for (item in items) {
        val derived = newDefaultIdlingSegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveDefaultIdlingSegment(item: DefaultIdlingSegment): String {
    return normalizeWeightedFuelBurnMappingCode(item.code)
}
