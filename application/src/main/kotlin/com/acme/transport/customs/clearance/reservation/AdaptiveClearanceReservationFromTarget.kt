package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.tariff.tariffscore.DefaultTariffTarget
import com.acme.transport.customs.tariff.tariffscore.normalizeAdaptiveTariffScoreCode

fun summarizeDefaultTariffTargetsForReservation(items: MutableList<DefaultTariffTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveAdaptiveClearanceReservationsFromTarget(items: MutableList<DefaultTariffTarget>): MutableList<AdaptiveClearanceReservation> {
    val out = mutableListOf<AdaptiveClearanceReservation>()
    for (item in items) {
        val derived = newAdaptiveClearanceReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveClearanceReservation(item: AdaptiveClearanceReservation): String {
    return normalizeAdaptiveTariffScoreCode(item.code)
}
