package com.acme.transport.crewing.availability.request

import com.acme.transport.crewing.swap.draft.ScheduledSwapNotice
import com.acme.transport.crewing.swap.draft.scheduledSwapNoticeWeight

fun summarizeScheduledSwapNoticesForTarget(items: MutableList<ScheduledSwapNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveAvailabilityTargetsFromNotice(items: MutableList<ScheduledSwapNotice>): MutableList<AvailabilityTarget> {
    val out = mutableListOf<AvailabilityTarget>()
    for (item in items) {
        val derived = newAvailabilityTarget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankAvailabilityTargetExtra(item: AvailabilityTarget): Double = scheduledSwapNoticeWeight(1, true)
