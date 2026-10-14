package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.lastmile.drop.weighteddropnotice.FallbackDropNotice
import com.acme.transport.lastmile.drop.weighteddropnotice.classifyDropSummary

fun summarizeFallbackDropNoticesForMapping(items: MutableList<FallbackDropNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveScheduledParcelMappingsFromNotice(items: MutableList<FallbackDropNotice>): MutableList<ScheduledParcelMapping> {
    val out = mutableListOf<ScheduledParcelMapping>()
    for (item in items) {
        val derived = newScheduledParcelMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveScheduledParcelMapping(item: ScheduledParcelMapping): String {
    return classifyDropSummary(item.measuredValue())
}
