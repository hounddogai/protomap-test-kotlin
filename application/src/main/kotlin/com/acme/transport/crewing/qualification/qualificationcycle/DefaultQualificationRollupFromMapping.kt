package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.availability.score.AvailabilityMapping
import com.acme.transport.crewing.availability.score.availabilityScoreWeight

fun summarizeAvailabilityMappingsForRollup(items: MutableList<AvailabilityMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultQualificationRollupsFromMapping(items: MutableList<AvailabilityMapping>): MutableList<DefaultQualificationRollup> {
    val out = mutableListOf<DefaultQualificationRollup>()
    for (item in items) {
        val derived = newDefaultQualificationRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyDefaultQualificationRollup(item: DefaultQualificationRollup): Double = availabilityScoreWeight(1, true)
