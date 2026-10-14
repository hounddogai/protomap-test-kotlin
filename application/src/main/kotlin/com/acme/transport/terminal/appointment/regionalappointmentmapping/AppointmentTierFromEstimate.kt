package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.terminal.crossdock.cappeddockestimate.CappedCrossDockEstimate
import com.acme.transport.terminal.crossdock.cappeddockestimate.classifyCappedCrossDockEstimate

fun summarizeCappedCrossDockEstimatesForTier(items: MutableList<CappedCrossDockEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveAppointmentTiersFromEstimate(items: MutableList<CappedCrossDockEstimate>): MutableList<AppointmentTier> {
    val out = mutableListOf<AppointmentTier>()
    for (item in items) {
        val derived = newAppointmentTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectAppointmentTier(item: AppointmentTier): String = classifyCappedCrossDockEstimate(item.netLoad())
