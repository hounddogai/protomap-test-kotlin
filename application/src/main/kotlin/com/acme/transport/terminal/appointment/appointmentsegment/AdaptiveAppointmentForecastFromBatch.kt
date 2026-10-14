package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.terminal.sortation.sortationadjustment.AdaptiveSortationBatch
import com.acme.transport.terminal.sortation.sortationadjustment.scheduledSortationAdjustmentBucket

fun summarizeAdaptiveSortationBatchesForForecast(items: MutableList<AdaptiveSortationBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveAppointmentForecastsFromBatch(items: MutableList<AdaptiveSortationBatch>): MutableList<AdaptiveAppointmentForecast> {
    val out = mutableListOf<AdaptiveAppointmentForecast>()
    for (item in items) {
        val derived = newAdaptiveAppointmentForecast(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveAppointmentForecast(item: AdaptiveAppointmentForecast): String {
    return scheduledSortationAdjustmentBucket(item.netLoad())
}
