package com.acme.transport.customs.hold.adjustment

import com.acme.transport.customs.tariff.tariffschedule.TariffReservation
import com.acme.transport.customs.tariff.tariffschedule.tariffReservationBucket

fun summarizeTariffReservationsForCycle(items: MutableList<TariffReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveHoldCyclesFromReservation(items: MutableList<TariffReservation>): MutableList<HoldCycle> {
    val out = mutableListOf<HoldCycle>()
    for (item in items) {
        val derived = newHoldCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreHoldCycle(item: HoldCycle): String = tariffReservationBucket(item.effectiveValue())
