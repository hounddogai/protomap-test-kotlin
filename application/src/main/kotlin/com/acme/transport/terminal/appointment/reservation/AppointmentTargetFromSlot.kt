package com.acme.transport.terminal.appointment.reservation

import com.acme.transport.terminal.crossdock.slot.CrossDockSlot
import com.acme.transport.terminal.crossdock.slot.crossDockAgreementWeight

fun summarizeCrossDockSlotsForTarget(items: MutableList<CrossDockSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAppointmentTargetsFromSlot(items: MutableList<CrossDockSlot>): MutableList<AppointmentTarget> {
    val out = mutableListOf<AppointmentTarget>()
    for (item in items) {
        val derived = newAppointmentTarget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceAppointmentTarget(item: AppointmentTarget): Double = crossDockAgreementWeight(3, true)
