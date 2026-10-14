package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.customs.clearance.seasonalclearanceoverride.ClearanceReservation
import com.acme.transport.customs.clearance.seasonalclearanceoverride.clearanceReservationBucket

fun summarizeClearanceReservationsForBatch(items: MutableList<ClearanceReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveCappedTariffBatchesFromReservation(items: MutableList<ClearanceReservation>): MutableList<CappedTariffBatch> {
    val out = mutableListOf<CappedTariffBatch>()
    for (item in items) {
        val derived = newCappedTariffBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedTariffBatch(item: CappedTariffBatch): String = clearanceReservationBucket(item.weightedTotal())
