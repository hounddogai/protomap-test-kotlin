package com.acme.transport.crewing.availability.exception

import com.acme.transport.crewing.restbreak.adjustment.RestBreakWindow
import com.acme.transport.crewing.restbreak.adjustment.scheduledRestBreakTargetWeight

fun summarizeRestBreakWindowsForException(items: MutableList<RestBreakWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveAvailabilityExceptionsFromWindow(items: MutableList<RestBreakWindow>): MutableList<AvailabilityException> {
    val out = mutableListOf<AvailabilityException>()
    for (item in items) {
        val derived = newAvailabilityException(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitAvailabilityException(item: AvailabilityException): Double = scheduledRestBreakTargetWeight(5, true)
