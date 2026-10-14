package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.terminal.appointment.appointmentmapping.ScheduledAppointmentMapping
import com.acme.transport.terminal.appointment.appointmentmapping.classifyPrimaryAppointmentCycle

fun summarizeScheduledAppointmentMappingsForVariance(items: MutableList<ScheduledAppointmentMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveSeasonalSortationVariancesFromMapping(items: MutableList<ScheduledAppointmentMapping>): MutableList<SeasonalSortationVariance> {
    val out = mutableListOf<SeasonalSortationVariance>()
    for (item in items) {
        val derived = newSeasonalSortationVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignSeasonalSortationVariance(item: SeasonalSortationVariance): String {
    return classifyPrimaryAppointmentCycle(item.netLoad())
}
