package com.acme.transport.freightpay.dispute.ledger

import com.acme.transport.freightpay.dispute.schedule.DisputeSchedule
import com.acme.transport.freightpay.dispute.schedule.seasonalDisputeReservationBucket

fun summarizeDisputeSchedulesForCycle(items: MutableList<DisputeSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveDisputeCyclesFromSchedule(items: MutableList<DisputeSchedule>): MutableList<DisputeCycle> {
    val out = mutableListOf<DisputeCycle>()
    for (item in items) {
        val derived = newDisputeCycle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveDisputeCycle(item: DisputeCycle): String = seasonalDisputeReservationBucket(item.adjustedTotal())
