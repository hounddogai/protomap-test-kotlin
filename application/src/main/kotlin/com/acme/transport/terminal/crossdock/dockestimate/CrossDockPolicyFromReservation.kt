package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.terminal.appointment.reservation.AppointmentReservation
import com.acme.transport.terminal.appointment.reservation.appointmentReservationWeight

fun summarizeAppointmentReservationsForPolicy(items: MutableList<AppointmentReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCrossDockPoliciesFromReservation(items: MutableList<AppointmentReservation>): MutableList<CrossDockPolicy> {
    val out = mutableListOf<CrossDockPolicy>()
    for (item in items) {
        val derived = newCrossDockPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCrossDockPolicy(item: CrossDockPolicy): Double = appointmentReservationWeight(2, true)
