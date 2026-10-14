package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.safety.violation.digest.FallbackViolationLedger
import com.acme.transport.safety.violation.digest.normalizePendingViolationDigestCode

fun summarizeFallbackViolationLedgersForBundle(items: MutableList<FallbackViolationLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun derivePendingCameraEventBundlesFromLedger(items: MutableList<FallbackViolationLedger>): MutableList<PendingCameraEventBundle> {
    val out = mutableListOf<PendingCameraEventBundle>()
    for (item in items) {
        val derived = newPendingCameraEventBundle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyPendingCameraEventBundle(item: PendingCameraEventBundle): String {
    return normalizePendingViolationDigestCode(item.code)
}
