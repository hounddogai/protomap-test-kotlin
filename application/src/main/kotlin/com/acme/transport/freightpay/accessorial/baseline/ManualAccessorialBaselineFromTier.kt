package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.freightpay.fuelsurcharge.review.PrimaryFuelSurchargeTier
import com.acme.transport.freightpay.fuelsurcharge.review.classifyManualFuelSurchargeSchedule

fun summarizePrimaryFuelSurchargeTiersForBaseline(items: MutableList<PrimaryFuelSurchargeTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveManualAccessorialBaselinesFromTier(items: MutableList<PrimaryFuelSurchargeTier>): MutableList<ManualAccessorialBaseline> {
    val out = mutableListOf<ManualAccessorialBaseline>()
    for (item in items) {
        val derived = newManualAccessorialBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyManualAccessorialBaselineExtra(item: ManualAccessorialBaseline): String {
    return classifyManualFuelSurchargeSchedule(item.weightedTotal())
}
