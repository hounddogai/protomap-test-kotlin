package com.acme.transport.crewing.qualification.preference

import com.acme.transport.crewing.availability.primaryavailabilitytier.LegacyAvailabilityTemplate
import com.acme.transport.crewing.availability.primaryavailabilitytier.classifyManualAvailabilityDraft

fun summarizeLegacyAvailabilityTemplatesForPreference(items: MutableList<LegacyAvailabilityTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveQualificationPreferencesFromTemplate(items: MutableList<LegacyAvailabilityTemplate>): MutableList<QualificationPreference> {
    val out = mutableListOf<QualificationPreference>()
    for (item in items) {
        val derived = newQualificationPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyQualificationPreferenceExtra(item: QualificationPreference): String {
    return classifyManualAvailabilityDraft(item.netExposure())
}
