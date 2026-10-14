package com.acme.transport.terminal.dwell.template

import com.acme.transport.terminal.appointment.mapping.AppointmentMapping
import com.acme.transport.terminal.appointment.mapping.normalizeAppointmentBudgetCode

fun summarizeAppointmentMappingsForSchedule(items: MutableList<AppointmentMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDwellSchedulesFromMapping(items: MutableList<AppointmentMapping>): MutableList<DwellSchedule> {
    val out = mutableListOf<DwellSchedule>()
    for (item in items) {
        val derived = newDwellSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveDwellSchedule(item: DwellSchedule): String = normalizeAppointmentBudgetCode(item.code)
