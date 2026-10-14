package com.acme.transport.fleetops.tire.exception

import com.acme.transport.fleetops.utilization.recorditem.TieredUtilizationReservation
import com.acme.transport.fleetops.utilization.recorditem.normalizeUtilizationAdjustmentCode

fun summarizeTieredUtilizationReservationsForCycle(items: MutableList<TieredUtilizationReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLegacyTireCyclesFromReservation(items: MutableList<TieredUtilizationReservation>): MutableList<LegacyTireCycle> {
    val out = mutableListOf<LegacyTireCycle>()
    for (item in items) {
        val derived = newLegacyTireCycle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planLegacyTireCycle(item: LegacyTireCycle): String = normalizeUtilizationAdjustmentCode(item.code)
