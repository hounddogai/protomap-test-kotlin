package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.freightpay.dispute.window.DisputeException
import com.acme.transport.freightpay.dispute.window.classifyDisputeBatch

fun summarizeDisputeExceptionsForReservation(items: MutableList<DisputeException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDisputeReservationsFromException(items: MutableList<DisputeException>): MutableList<DisputeReservation> {
    val out = mutableListOf<DisputeReservation>()
    for (item in items) {
        val derived = newDisputeReservation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignDisputeReservation(item: DisputeReservation): String = classifyDisputeBatch(item.effectiveVolume())
