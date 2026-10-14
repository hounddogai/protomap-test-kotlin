package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.terminal.appointment.reservation.AppointmentReservation
import com.acme.transport.terminal.appointment.reservation.appointmentReservationWeight

fun summarizeAppointmentReservationsForRollup(items: MutableList<AppointmentReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDockDoorRollupsFromReservation(items: MutableList<AppointmentReservation>): MutableList<DockDoorRollup> {
    val out = mutableListOf<DockDoorRollup>()
    for (item in items) {
        val derived = newDockDoorRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDockDoorRollup(item: DockDoorRollup): Double = appointmentReservationWeight(3, true)
