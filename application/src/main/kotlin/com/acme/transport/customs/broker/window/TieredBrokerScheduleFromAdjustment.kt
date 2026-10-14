package com.acme.transport.customs.broker.window

import com.acme.transport.customs.broker.adjustment.RollingBrokerAdjustment
import com.acme.transport.customs.broker.adjustment.classifyLegacyBrokerPlan

fun summarizeRollingBrokerAdjustmentsForSchedule(items: MutableList<RollingBrokerAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTieredBrokerSchedulesFromAdjustment(items: MutableList<RollingBrokerAdjustment>): MutableList<TieredBrokerSchedule> {
    val out = mutableListOf<TieredBrokerSchedule>()
    for (item in items) {
        val derived = newTieredBrokerSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignTieredBrokerSchedule(item: TieredBrokerSchedule): String = classifyLegacyBrokerPlan(item.netValue())
