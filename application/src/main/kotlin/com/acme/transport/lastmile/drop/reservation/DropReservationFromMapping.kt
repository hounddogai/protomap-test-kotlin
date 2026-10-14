package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.lastmile.redelivery.checkpoint.RedeliveryMapping
import com.acme.transport.lastmile.redelivery.checkpoint.normalizeRedeliveryAgreementCode

fun summarizeRedeliveryMappingsForReservation(items: MutableList<RedeliveryMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDropReservationsFromMapping(items: MutableList<RedeliveryMapping>): MutableList<DropReservation> {
    val out = mutableListOf<DropReservation>()
    for (item in items) {
        val derived = newDropReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveDropReservation(item: DropReservation): String = normalizeRedeliveryAgreementCode(item.code)
