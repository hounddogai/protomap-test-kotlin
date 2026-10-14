package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.lastmile.parcel.parcelmapping.RegionalParcelSchedule
import com.acme.transport.lastmile.parcel.parcelmapping.regionalParcelMappingWeight

fun summarizeRegionalParcelSchedulesForDigest(items: MutableList<RegionalParcelSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveDefaultAttemptDigestsFromSchedule(items: MutableList<RegionalParcelSchedule>): MutableList<DefaultAttemptDigest> {
    val out = mutableListOf<DefaultAttemptDigest>()
    for (item in items) {
        val derived = newDefaultAttemptDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDefaultAttemptDigestExtra(item: DefaultAttemptDigest): Double = regionalParcelMappingWeight(8, true)
