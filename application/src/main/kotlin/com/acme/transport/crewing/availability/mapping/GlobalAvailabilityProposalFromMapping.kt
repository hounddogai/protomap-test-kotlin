package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.dutyperiod.mapping.LegacyDutyPeriodMapping
import com.acme.transport.crewing.dutyperiod.mapping.legacyDutyPeriodMappingBucket

fun summarizeLegacyDutyPeriodMappingsForProposal(items: MutableList<LegacyDutyPeriodMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalAvailabilityProposalsFromMapping(items: MutableList<LegacyDutyPeriodMapping>): MutableList<GlobalAvailabilityProposal> {
    val out = mutableListOf<GlobalAvailabilityProposal>()
    for (item in items) {
        val derived = newGlobalAvailabilityProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectGlobalAvailabilityProposal(item: GlobalAvailabilityProposal): String {
    return legacyDutyPeriodMappingBucket(item.effectiveTotal())
}
