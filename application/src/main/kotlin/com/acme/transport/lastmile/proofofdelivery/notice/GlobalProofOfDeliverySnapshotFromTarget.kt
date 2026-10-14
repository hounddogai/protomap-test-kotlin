package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.lastmile.parcel.schedule.FallbackParcelTarget
import com.acme.transport.lastmile.parcel.schedule.classifyFallbackParcelTarget

fun summarizeFallbackParcelTargetsForSnapshot(items: MutableList<FallbackParcelTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalProofOfDeliverySnapshotsFromTarget(items: MutableList<FallbackParcelTarget>): MutableList<GlobalProofOfDeliverySnapshot> {
    val out = mutableListOf<GlobalProofOfDeliverySnapshot>()
    for (item in items) {
        val derived = newGlobalProofOfDeliverySnapshot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectGlobalProofOfDeliverySnapshot(item: GlobalProofOfDeliverySnapshot): String {
    return classifyFallbackParcelTarget(item.netVolume())
}
