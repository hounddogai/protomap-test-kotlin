package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.terminal.traileryard.yardreservation.SeasonalTrailerYardReservation
import com.acme.transport.terminal.traileryard.yardreservation.classifyScheduledTrailerYardScore

fun summarizeSeasonalTrailerYardReservationsForException(items: MutableList<SeasonalTrailerYardReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingAppointmentExceptionsFromReservation(items: MutableList<SeasonalTrailerYardReservation>): MutableList<RollingAppointmentException> {
    val out = mutableListOf<RollingAppointmentException>()
    for (item in items) {
        val derived = newRollingAppointmentException(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignRollingAppointmentException(item: RollingAppointmentException): String {
    return classifyScheduledTrailerYardScore(item.netVolume())
}
