package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.safety.cameraevent.ledger.CameraEventEstimate
import com.acme.transport.safety.cameraevent.ledger.weightedCameraEventLedgerBucket

fun summarizeCameraEventEstimatesForCycle(items: MutableList<CameraEventEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveNearMissCyclesFromEstimate(items: MutableList<CameraEventEstimate>): MutableList<NearMissCycle> {
    val out = mutableListOf<NearMissCycle>()
    for (item in items) {
        val derived = newNearMissCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreNearMissCycle(item: NearMissCycle): String = weightedCameraEventLedgerBucket(item.projectedLoad())
