package com.acme.transport.crewing.availability.agreement

import com.acme.transport.crewing.dutyperiod.preference.DutyPeriodPreference
import com.acme.transport.crewing.dutyperiod.preference.normalizeGlobalDutyPeriodRecordCode

fun summarizeDutyPeriodPreferencesForDigest(items: MutableList<DutyPeriodPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAvailabilityDigestsFromPreference(items: MutableList<DutyPeriodPreference>): MutableList<AvailabilityDigest> {
    val out = mutableListOf<AvailabilityDigest>()
    for (item in items) {
        val derived = newAvailabilityDigest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAvailabilityDigest(item: AvailabilityDigest): String = normalizeGlobalDutyPeriodRecordCode(item.code)
