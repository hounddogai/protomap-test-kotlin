package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.terminal.appointment.score.LenientAppointmentMetric
import com.acme.transport.terminal.appointment.score.lenientAppointmentMetricBucket

fun summarizeLenientAppointmentMetricsForBaseline(items: MutableList<LenientAppointmentMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveDockDoorBaselinesFromMetric(items: MutableList<LenientAppointmentMetric>): MutableList<DockDoorBaseline> {
    val out = mutableListOf<DockDoorBaseline>()
    for (item in items) {
        val derived = newDockDoorBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignDockDoorBaseline(item: DockDoorBaseline): String = lenientAppointmentMetricBucket(item.measuredValue())
