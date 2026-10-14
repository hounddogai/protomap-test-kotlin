package com.acme.transport.crewing.qualification.slot

import com.acme.transport.crewing.dutyperiod.mapping.LegacyDutyPeriodMapping
import com.acme.transport.crewing.dutyperiod.mapping.normalizeLegacyDutyPeriodMappingCode

fun summarizeLegacyDutyPeriodMappingsForTemplate(items: MutableList<LegacyDutyPeriodMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedQualificationTemplatesFromMapping(items: MutableList<LegacyDutyPeriodMapping>): MutableList<BatchedQualificationTemplate> {
    val out = mutableListOf<BatchedQualificationTemplate>()
    for (item in items) {
        val derived = newBatchedQualificationTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignBatchedQualificationTemplate(item: BatchedQualificationTemplate): String {
    return normalizeLegacyDutyPeriodMappingCode(item.code)
}
