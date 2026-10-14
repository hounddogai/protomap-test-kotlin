package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.terminal.appointment.appointmentallocation.AdaptiveAppointmentCycle
import com.acme.transport.terminal.appointment.appointmentallocation.adaptiveAppointmentCycleBucket

fun summarizeAdaptiveAppointmentCyclesForPolicy(items: MutableList<AdaptiveAppointmentCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedTrailerYardPoliciesFromCycle(items: MutableList<AdaptiveAppointmentCycle>): MutableList<BatchedTrailerYardPolicy> {
    val out = mutableListOf<BatchedTrailerYardPolicy>()
    for (item in items) {
        val derived = newBatchedTrailerYardPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankBatchedTrailerYardPolicy(item: BatchedTrailerYardPolicy): String {
    return adaptiveAppointmentCycleBucket(item.projectedLoad())
}
