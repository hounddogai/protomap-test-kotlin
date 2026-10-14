package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.freightpay.accessorial.entry.RollingAccessorialAllocation
import com.acme.transport.freightpay.accessorial.entry.classifyRollingAccessorialAllocation

fun summarizeRollingAccessorialAllocationsForBatch(items: MutableList<RollingAccessorialAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFuelSurchargeBatchesFromAllocation(items: MutableList<RollingAccessorialAllocation>): MutableList<FuelSurchargeBatch> {
    val out = mutableListOf<FuelSurchargeBatch>()
    for (item in items) {
        val derived = newFuelSurchargeBatch(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyFuelSurchargeBatch(item: FuelSurchargeBatch): String {
    return classifyRollingAccessorialAllocation(item.adjustedVolume())
}
