package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.terminal.appointment.exception.AppointmentException
import com.acme.transport.terminal.appointment.exception.classifyAppointmentException

fun summarizeAppointmentExceptionsForReservation(items: MutableList<AppointmentException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveBatchedTrailerYardReservationsFromException(items: MutableList<AppointmentException>): MutableList<BatchedTrailerYardReservation> {
    val out = mutableListOf<BatchedTrailerYardReservation>()
    for (item in items) {
        val derived = newBatchedTrailerYardReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBatchedTrailerYardReservation(item: BatchedTrailerYardReservation): String {
    return classifyAppointmentException(item.weightedTotal())
}
