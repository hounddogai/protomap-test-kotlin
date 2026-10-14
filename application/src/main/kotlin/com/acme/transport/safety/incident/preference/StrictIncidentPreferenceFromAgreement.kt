package com.acme.transport.safety.incident.preference

import com.acme.transport.safety.coaching.agreement.RegionalCoachingAgreement
import com.acme.transport.safety.coaching.agreement.regionalCoachingAgreementWeight

fun summarizeRegionalCoachingAgreementsForPreference(items: MutableList<RegionalCoachingAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictIncidentPreferencesFromAgreement(items: MutableList<RegionalCoachingAgreement>): MutableList<StrictIncidentPreference> {
    val out = mutableListOf<StrictIncidentPreference>()
    for (item in items) {
        val derived = newStrictIncidentPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceStrictIncidentPreference(item: StrictIncidentPreference): Double {
    return regionalCoachingAgreementWeight(1, true)
}
