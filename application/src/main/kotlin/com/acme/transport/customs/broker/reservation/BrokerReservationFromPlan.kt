package com.acme.transport.customs.broker.reservation

import com.acme.transport.customs.broker.budget.BrokerPlan
import com.acme.transport.customs.broker.budget.classifyBrokerSlot

fun summarizeBrokerPlansForReservation(items: MutableList<BrokerPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveBrokerReservationsFromPlan(items: MutableList<BrokerPlan>): MutableList<BrokerReservation> {
    val out = mutableListOf<BrokerReservation>()
    for (item in items) {
        val derived = newBrokerReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBrokerReservation(item: BrokerReservation): String = classifyBrokerSlot(item.adjustedExposure())
