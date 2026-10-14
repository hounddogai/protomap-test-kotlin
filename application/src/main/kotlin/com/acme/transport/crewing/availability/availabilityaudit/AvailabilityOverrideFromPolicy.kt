package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.crewing.qualification.overrideitem.QualificationPolicy
import com.acme.transport.crewing.qualification.overrideitem.qualificationPolicyWeight

fun summarizeQualificationPoliciesForOverride(items: MutableList<QualificationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveAvailabilityOverridesFromPolicy(items: MutableList<QualificationPolicy>): MutableList<AvailabilityOverride> {
    val out = mutableListOf<AvailabilityOverride>()
    for (item in items) {
        val derived = newAvailabilityOverride(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeAvailabilityOverride(item: AvailabilityOverride): Double = qualificationPolicyWeight(4, true)
