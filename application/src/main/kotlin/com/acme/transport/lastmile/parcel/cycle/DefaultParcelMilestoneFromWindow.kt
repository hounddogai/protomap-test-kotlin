package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.lastmile.proofofdelivery.window.ProofOfDeliveryWindow
import com.acme.transport.lastmile.proofofdelivery.window.classifyProofOfDeliveryRollup

fun summarizeProofOfDeliveryWindowsForMilestone(items: MutableList<ProofOfDeliveryWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveDefaultParcelMilestonesFromWindow(items: MutableList<ProofOfDeliveryWindow>): MutableList<DefaultParcelMilestone> {
    val out = mutableListOf<DefaultParcelMilestone>()
    for (item in items) {
        val derived = newDefaultParcelMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectDefaultParcelMilestone(item: DefaultParcelMilestone): String {
    return classifyProofOfDeliveryRollup(item.adjustedExposure())
}
