package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.freightpay.fuelsurcharge.group.LenientFuelSurchargeAssignment
import com.acme.transport.freightpay.fuelsurcharge.group.classifyLenientFuelSurchargeAssignment

fun summarizeLenientFuelSurchargeAssignmentsForAllocation(items: MutableList<LenientFuelSurchargeAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveWeightedAccessorialAllocationsFromAssignment(items: MutableList<LenientFuelSurchargeAssignment>): MutableList<WeightedAccessorialAllocation> {
    val out = mutableListOf<WeightedAccessorialAllocation>()
    for (item in items) {
        val derived = newWeightedAccessorialAllocation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planWeightedAccessorialAllocation(item: WeightedAccessorialAllocation): String {
    return classifyLenientFuelSurchargeAssignment(item.weightedLoad())
}
