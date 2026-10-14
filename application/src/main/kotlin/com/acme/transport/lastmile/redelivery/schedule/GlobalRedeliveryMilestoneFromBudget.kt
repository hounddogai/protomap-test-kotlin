package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.lastmile.parcel.reservation.ScheduledParcelBudget
import com.acme.transport.lastmile.parcel.reservation.classifyLegacyParcelAdjustment

fun summarizeScheduledParcelBudgetsForMilestone(items: MutableList<ScheduledParcelBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveGlobalRedeliveryMilestonesFromBudget(items: MutableList<ScheduledParcelBudget>): MutableList<GlobalRedeliveryMilestone> {
    val out = mutableListOf<GlobalRedeliveryMilestone>()
    for (item in items) {
        val derived = newGlobalRedeliveryMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectGlobalRedeliveryMilestone(item: GlobalRedeliveryMilestone): String {
    return classifyLegacyParcelAdjustment(item.measuredValue())
}
