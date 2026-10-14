package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.dispute.reservation.LegacyDisputeRequest
import com.acme.transport.freightpay.dispute.reservation.classifyFallbackDisputeRollup

fun summarizeLegacyDisputeRequestsForMilestone(items: MutableList<LegacyDisputeRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveFuelSurchargeMilestonesFromRequest(items: MutableList<LegacyDisputeRequest>): MutableList<FuelSurchargeMilestone> {
    val out = mutableListOf<FuelSurchargeMilestone>()
    for (item in items) {
        val derived = newFuelSurchargeMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignFuelSurchargeMilestone(item: FuelSurchargeMilestone): String {
    return classifyFallbackDisputeRollup(item.projectedExposure())
}
