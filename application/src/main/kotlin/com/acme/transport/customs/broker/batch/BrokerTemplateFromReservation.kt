package com.acme.transport.customs.broker.batch

import com.acme.transport.customs.inspection.forecast.RollingInspectionReservation
import com.acme.transport.customs.inspection.forecast.legacyInspectionForecastWeight

fun summarizeRollingInspectionReservationsForTemplate(items: MutableList<RollingInspectionReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveBrokerTemplatesFromReservation(items: MutableList<RollingInspectionReservation>): MutableList<BrokerTemplate> {
    val out = mutableListOf<BrokerTemplate>()
    for (item in items) {
        val derived = newBrokerTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleBrokerTemplate(item: BrokerTemplate): Double = legacyInspectionForecastWeight(1, true)
