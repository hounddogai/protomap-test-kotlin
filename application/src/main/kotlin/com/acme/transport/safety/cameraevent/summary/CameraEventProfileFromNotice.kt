package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.safety.coaching.notice.CappedCoachingNotice
import com.acme.transport.safety.coaching.notice.classifyCoachingNotice

fun summarizeCappedCoachingNoticesForProfile(items: MutableList<CappedCoachingNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCameraEventProfilesFromNotice(items: MutableList<CappedCoachingNotice>): MutableList<CameraEventProfile> {
    val out = mutableListOf<CameraEventProfile>()
    for (item in items) {
        val derived = newCameraEventProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreCameraEventProfile(item: CameraEventProfile): String = classifyCoachingNotice(item.effectiveValue())
