package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment.RegionalFuelSurchargeAdjustment
import com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment.classifyFuelSurchargeBatch

fun summarizeRegionalFuelSurchargeAdjustmentsForEntry(items: MutableList<RegionalFuelSurchargeAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveCappedAccessorialEntriesFromAdjustment(items: MutableList<RegionalFuelSurchargeAdjustment>): MutableList<CappedAccessorialEntry> {
    val out = mutableListOf<CappedAccessorialEntry>()
    for (item in items) {
        val derived = newCappedAccessorialEntry(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveCappedAccessorialEntry(item: CappedAccessorialEntry): String {
    return classifyFuelSurchargeBatch(item.projectedLoad())
}
