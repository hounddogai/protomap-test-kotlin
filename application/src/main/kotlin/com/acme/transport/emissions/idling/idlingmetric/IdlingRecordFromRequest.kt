package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.emissions.fuelburn.request.FuelBurnRequest
import com.acme.transport.emissions.fuelburn.request.normalizePrimaryFuelBurnRequestCode

fun summarizeFuelBurnRequestsForRecord(items: MutableList<FuelBurnRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveIdlingRecordsFromRequest(items: MutableList<FuelBurnRequest>): MutableList<IdlingRecord> {
    val out = mutableListOf<IdlingRecord>()
    for (item in items) {
        val derived = newIdlingRecord(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileIdlingRecord(item: IdlingRecord): String = normalizePrimaryFuelBurnRequestCode(item.code)
