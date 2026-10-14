package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.freightpay.dispute.group.GlobalDisputeReservation
import com.acme.transport.freightpay.dispute.group.disputeQuotaBucket

fun summarizeGlobalDisputeReservationsForSegment(items: MutableList<GlobalDisputeReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFuelSurchargeSegmentsFromReservation(items: MutableList<GlobalDisputeReservation>): MutableList<FuelSurchargeSegment> {
    val out = mutableListOf<FuelSurchargeSegment>()
    for (item in items) {
        val derived = newFuelSurchargeSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchFuelSurchargeSegment(item: FuelSurchargeSegment): String = disputeQuotaBucket(item.netTotal())
