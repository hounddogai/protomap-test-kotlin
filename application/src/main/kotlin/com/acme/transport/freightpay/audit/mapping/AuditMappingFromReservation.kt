package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.freightpay.ratecard.reservation.PrimaryRateCardReservation
import com.acme.transport.freightpay.ratecard.reservation.strictRateCardEntryWeight

fun summarizePrimaryRateCardReservationsForMapping(items: MutableList<PrimaryRateCardReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAuditMappingsFromReservation(items: MutableList<PrimaryRateCardReservation>): MutableList<AuditMapping> {
    val out = mutableListOf<AuditMapping>()
    for (item in items) {
        val derived = newAuditMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchAuditMapping(item: AuditMapping): Double = strictRateCardEntryWeight(2, true)
