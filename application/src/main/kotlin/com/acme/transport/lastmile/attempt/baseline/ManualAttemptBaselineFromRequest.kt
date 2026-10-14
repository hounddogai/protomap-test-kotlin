package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.lastmile.parcel.request.ParcelRequest
import com.acme.transport.lastmile.parcel.request.normalizeParcelRequestCode

fun summarizeParcelRequestsForBaseline(items: MutableList<ParcelRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveManualAttemptBaselinesFromRequest(items: MutableList<ParcelRequest>): MutableList<ManualAttemptBaseline> {
    val out = mutableListOf<ManualAttemptBaseline>()
    for (item in items) {
        val derived = newManualAttemptBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeManualAttemptBaseline(item: ManualAttemptBaseline): String = normalizeParcelRequestCode(item.code)
