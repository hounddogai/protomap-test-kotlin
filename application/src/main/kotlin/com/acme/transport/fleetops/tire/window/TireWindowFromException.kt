package com.acme.transport.fleetops.tire.window

import com.acme.transport.fleetops.inspection.schedule.InspectionException
import com.acme.transport.fleetops.inspection.schedule.classifyInspectionException

fun summarizeInspectionExceptionsForWindow(items: MutableList<InspectionException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveTireWindowsFromException(items: MutableList<InspectionException>): MutableList<TireWindow> {
    val out = mutableListOf<TireWindow>()
    for (item in items) {
        val derived = newTireWindow(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankTireWindow(item: TireWindow): String = classifyInspectionException(item.measuredValue())
