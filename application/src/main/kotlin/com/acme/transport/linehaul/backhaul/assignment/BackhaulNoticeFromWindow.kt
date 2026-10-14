package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.linehaul.backhaul.threshold.BackhaulWindow
import com.acme.transport.linehaul.backhaul.threshold.backhaulThresholdBucket

fun summarizeBackhaulWindowsForNotice(items: MutableList<BackhaulWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveBackhaulNoticesFromWindow(items: MutableList<BackhaulWindow>): MutableList<BackhaulNotice> {
    val out = mutableListOf<BackhaulNotice>()
    for (item in items) {
        val derived = newBackhaulNotice(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBackhaulNotice(item: BackhaulNotice): String = backhaulThresholdBucket(item.netLoad())
