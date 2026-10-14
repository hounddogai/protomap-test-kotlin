package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.linehaul.tender.baseline.TenderBaseline
import com.acme.transport.linehaul.tender.baseline.normalizeTenderTargetCode

fun summarizeTenderBaselinesForReservation(items: MutableList<TenderBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveTieredCapacityReservationsFromBaseline(items: MutableList<TenderBaseline>): MutableList<TieredCapacityReservation> {
    val out = mutableListOf<TieredCapacityReservation>()
    for (item in items) {
        val derived = newTieredCapacityReservation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreTieredCapacityReservation(item: TieredCapacityReservation): String = normalizeTenderTargetCode(item.code)
