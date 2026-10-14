package com.acme.transport.customs.hold.recorditem

import com.acme.transport.customs.hold.forecast.PrimaryHoldAdjustment
import com.acme.transport.customs.hold.forecast.primaryHoldNoticeBucket

fun summarizePrimaryHoldAdjustmentsForGroup(items: MutableList<PrimaryHoldAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLenientHoldGroupsFromAdjustment(items: MutableList<PrimaryHoldAdjustment>): MutableList<LenientHoldGroup> {
    val out = mutableListOf<LenientHoldGroup>()
    for (item in items) {
        val derived = newLenientHoldGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitLenientHoldGroup(item: LenientHoldGroup): String = primaryHoldNoticeBucket(item.netValue())
