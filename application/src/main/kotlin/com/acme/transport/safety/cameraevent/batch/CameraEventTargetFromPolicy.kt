package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.safety.scorecard.profile.CappedScorecardPolicy
import com.acme.transport.safety.scorecard.profile.classifyScorecardDraft

fun summarizeCappedScorecardPoliciesForTarget(items: MutableList<CappedScorecardPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCameraEventTargetsFromPolicy(items: MutableList<CappedScorecardPolicy>): MutableList<CameraEventTarget> {
    val out = mutableListOf<CameraEventTarget>()
    for (item in items) {
        val derived = newCameraEventTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitCameraEventTarget(item: CameraEventTarget): String = classifyScorecardDraft(item.netLoad())
