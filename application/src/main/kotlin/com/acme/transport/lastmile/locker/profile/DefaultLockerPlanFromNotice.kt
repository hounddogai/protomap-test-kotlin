package com.acme.transport.lastmile.locker.profile

import com.acme.transport.lastmile.redelivery.overrideitem.SeasonalRedeliveryNotice
import com.acme.transport.lastmile.redelivery.overrideitem.redeliveryOverrideBucket

fun summarizeSeasonalRedeliveryNoticesForPlan(items: MutableList<SeasonalRedeliveryNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultLockerPlansFromNotice(items: MutableList<SeasonalRedeliveryNotice>): MutableList<DefaultLockerPlan> {
    val out = mutableListOf<DefaultLockerPlan>()
    for (item in items) {
        val derived = newDefaultLockerPlan(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankDefaultLockerPlan(item: DefaultLockerPlan): String = redeliveryOverrideBucket(item.effectiveVolume())
