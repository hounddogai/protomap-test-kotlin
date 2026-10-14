package com.acme.transport.safety.scorecard.profile

import com.acme.transport.safety.cameraevent.policy.CameraEventDigest
import com.acme.transport.safety.cameraevent.policy.normalizeManualCameraEventPolicyCode

fun summarizeCameraEventDigestsForPolicy(items: MutableList<CameraEventDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveCappedScorecardPoliciesFromDigest(items: MutableList<CameraEventDigest>): MutableList<CappedScorecardPolicy> {
    val out = mutableListOf<CappedScorecardPolicy>()
    for (item in items) {
        val derived = newCappedScorecardPolicy(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchCappedScorecardPolicy(item: CappedScorecardPolicy): String {
    return normalizeManualCameraEventPolicyCode(item.code)
}
