package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.freightpay.accessorial.accessorialcycle.RegionalAccessorialCycle
import com.acme.transport.freightpay.accessorial.accessorialcycle.accessorialMilestoneWeight

fun summarizeRegionalAccessorialCyclesForSegment(items: MutableList<RegionalAccessorialCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveFuelSurchargeSegmentsFromCycle(items: MutableList<RegionalAccessorialCycle>): MutableList<FuelSurchargeSegment> {
    val out = mutableListOf<FuelSurchargeSegment>()
    for (item in items) {
        val derived = newFuelSurchargeSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreFuelSurchargeSegment(item: FuelSurchargeSegment): Double = accessorialMilestoneWeight(2, true)
