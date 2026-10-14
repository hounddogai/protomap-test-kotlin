package com.acme.transport.terminal.appointment.policy

import com.acme.transport.terminal.dockdoor.doorrecord.RollingDockDoorForecast
import com.acme.transport.terminal.dockdoor.doorrecord.normalizeRollingDockDoorForecastCode

fun summarizeRollingDockDoorForecastsForSlot(items: MutableList<RollingDockDoorForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAppointmentSlotsFromForecast(items: MutableList<RollingDockDoorForecast>): MutableList<AppointmentSlot> {
    val out = mutableListOf<AppointmentSlot>()
    for (item in items) {
        val derived = newAppointmentSlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectAppointmentSlot(item: AppointmentSlot): String = normalizeRollingDockDoorForecastCode(item.code)
