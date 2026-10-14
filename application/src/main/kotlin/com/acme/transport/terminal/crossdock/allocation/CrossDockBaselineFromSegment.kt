package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.terminal.appointment.snapshot.WeightedAppointmentSegment
import com.acme.transport.terminal.appointment.snapshot.appointmentLedgerBucket

fun summarizeWeightedAppointmentSegmentsForBaseline(items: MutableList<WeightedAppointmentSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCrossDockBaselinesFromSegment(items: MutableList<WeightedAppointmentSegment>): MutableList<CrossDockBaseline> {
    val out = mutableListOf<CrossDockBaseline>()
    for (item in items) {
        val derived = newCrossDockBaseline(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreCrossDockBaseline(item: CrossDockBaseline): String = appointmentLedgerBucket(item.measuredValue())
