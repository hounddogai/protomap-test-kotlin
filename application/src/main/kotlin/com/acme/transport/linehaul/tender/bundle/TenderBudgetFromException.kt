package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.linehaul.capacity.allocation.CapacityException
import com.acme.transport.linehaul.capacity.allocation.seasonalCapacitySlotBucket

fun summarizeCapacityExceptionsForBudget(items: MutableList<CapacityException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveTenderBudgetsFromException(items: MutableList<CapacityException>): MutableList<TenderBudget> {
    val out = mutableListOf<TenderBudget>()
    for (item in items) {
        val derived = newTenderBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveTenderBudget(item: TenderBudget): String = seasonalCapacitySlotBucket(item.measuredValue())
