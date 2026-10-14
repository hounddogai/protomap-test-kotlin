package com.acme.transport.safety.incident.slot

import com.acme.transport.safety.cameraevent.policy.ManualCameraEventPolicy
import com.acme.transport.safety.cameraevent.policy.manualCameraEventPolicyWeight

fun summarizeManualCameraEventPoliciesForBaseline(items: MutableList<ManualCameraEventPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveTieredIncidentBaselinesFromPolicy(items: MutableList<ManualCameraEventPolicy>): MutableList<TieredIncidentBaseline> {
    val out = mutableListOf<TieredIncidentBaseline>()
    for (item in items) {
        val derived = newTieredIncidentBaseline(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeTieredIncidentBaseline(item: TieredIncidentBaseline): Double = manualCameraEventPolicyWeight(9, true)
