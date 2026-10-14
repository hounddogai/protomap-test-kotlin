package com.acme.transport.crewing.qualification.target

import com.acme.transport.crewing.restbreak.snapshot.RestBreakSnapshot
import com.acme.transport.crewing.restbreak.snapshot.classifyRestBreakThreshold

fun summarizeRestBreakSnapshotsForReservation(items: MutableList<RestBreakSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveQualificationReservationsFromSnapshot(items: MutableList<RestBreakSnapshot>): MutableList<QualificationReservation> {
    val out = mutableListOf<QualificationReservation>()
    for (item in items) {
        val derived = newQualificationReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankQualificationReservation(item: QualificationReservation): String {
    return classifyRestBreakThreshold(item.adjustedLoad())
}
