package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.network.servicearea.areacycle.ScheduledServiceAreaRule
import com.acme.transport.network.servicearea.areacycle.classifyBatchedServiceAreaCycle

fun summarizeScheduledServiceAreaRulesForAdjustment(items: MutableList<ScheduledServiceAreaRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictServiceAreaAdjustmentsFromRule(items: MutableList<ScheduledServiceAreaRule>): MutableList<StrictServiceAreaAdjustment> {
    val out = mutableListOf<StrictServiceAreaAdjustment>()
    for (item in items) {
        val derived = newStrictServiceAreaAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyStrictServiceAreaAdjustmentExtra(item: StrictServiceAreaAdjustment): String {
    return classifyBatchedServiceAreaCycle(item.netExposure())
}
