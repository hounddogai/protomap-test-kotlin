package com.acme.transport.network.zone.schedule

import com.acme.transport.network.cutoff.globalcutoffledger.TieredCutoffReservation
import com.acme.transport.network.cutoff.globalcutoffledger.batchedCutoffLedgerWeight

fun summarizeTieredCutoffReservationsForTarget(items: MutableList<TieredCutoffReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCappedZoneTargetsFromReservation(items: MutableList<TieredCutoffReservation>): MutableList<CappedZoneTarget> {
    val out = mutableListOf<CappedZoneTarget>()
    for (item in items) {
        val derived = newCappedZoneTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planCappedZoneTarget(item: CappedZoneTarget): Double = batchedCutoffLedgerWeight(6, true)
