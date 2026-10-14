package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.safety.cameraevent.bundle.CameraEventBaseline
import com.acme.transport.safety.cameraevent.bundle.cameraEventBundleBucket

fun summarizeCameraEventBaselinesForProfile(items: MutableList<CameraEventBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRollingScorecardProfilesFromBaseline(items: MutableList<CameraEventBaseline>): MutableList<RollingScorecardProfile> {
    val out = mutableListOf<RollingScorecardProfile>()
    for (item in items) {
        val derived = newRollingScorecardProfile(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreRollingScorecardProfile(item: RollingScorecardProfile): String {
    return cameraEventBundleBucket(item.adjustedVolume())
}
