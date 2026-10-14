package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.freightpay.accessorial.scheduledaccessorialplan.SeasonalAccessorialAllocation
import com.acme.transport.freightpay.accessorial.scheduledaccessorialplan.normalizeSeasonalAccessorialPlanCode

fun summarizeSeasonalAccessorialAllocationsForSnapshot(items: MutableList<SeasonalAccessorialAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRegionalFuelSurchargeSnapshotsFromAllocation(items: MutableList<SeasonalAccessorialAllocation>): MutableList<RegionalFuelSurchargeSnapshot> {
    val out = mutableListOf<RegionalFuelSurchargeSnapshot>()
    for (item in items) {
        val derived = newRegionalFuelSurchargeSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitRegionalFuelSurchargeSnapshot(item: RegionalFuelSurchargeSnapshot): String {
    return normalizeSeasonalAccessorialPlanCode(item.code)
}
