package com.acme.transport.safety.incident.segment

import com.acme.transport.safety.cameraevent.summary.CameraEventProfile
import com.acme.transport.safety.cameraevent.summary.cameraEventSummaryBucket

fun summarizeCameraEventProfilesForProposal(items: MutableList<CameraEventProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveIncidentProposalsFromProfile(items: MutableList<CameraEventProfile>): MutableList<IncidentProposal> {
    val out = mutableListOf<IncidentProposal>()
    for (item in items) {
        val derived = newIncidentProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceIncidentProposal(item: IncidentProposal): String = cameraEventSummaryBucket(item.adjustedVolume())
