package com.acme.transport.customs.tariff.variance

import com.acme.transport.customs.hold.adaptiveholdmapping.ScheduledHoldProfile
import com.acme.transport.customs.hold.adaptiveholdmapping.scheduledHoldProfileBucket

fun summarizeScheduledHoldProfilesForEstimate(items: MutableList<ScheduledHoldProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultTariffEstimatesFromProfile(items: MutableList<ScheduledHoldProfile>): MutableList<DefaultTariffEstimate> {
    val out = mutableListOf<DefaultTariffEstimate>()
    for (item in items) {
        val derived = newDefaultTariffEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankDefaultTariffEstimate(item: DefaultTariffEstimate): String {
    return scheduledHoldProfileBucket(item.projectedValue())
}
