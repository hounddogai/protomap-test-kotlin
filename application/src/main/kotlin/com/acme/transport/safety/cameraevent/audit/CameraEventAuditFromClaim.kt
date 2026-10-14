package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.safety.coaching.target.CoachingClaim
import com.acme.transport.safety.coaching.target.normalizeCoachingManifestCode

fun summarizeCoachingClaimsForAudit(items: MutableList<CoachingClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCameraEventAuditsFromClaim(items: MutableList<CoachingClaim>): MutableList<CameraEventAudit> {
    val out = mutableListOf<CameraEventAudit>()
    for (item in items) {
        val derived = newCameraEventAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreCameraEventAudit(item: CameraEventAudit): String = normalizeCoachingManifestCode(item.code)
