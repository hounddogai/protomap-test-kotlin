package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.emissions.carbonreport.reportmapping.PrimaryCarbonReportTemplate
import com.acme.transport.emissions.carbonreport.reportmapping.globalCarbonReportAdjustmentBucket

fun summarizePrimaryCarbonReportTemplatesForProfile(items: MutableList<PrimaryCarbonReportTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualRouteOffsetProfilesFromTemplate(items: MutableList<PrimaryCarbonReportTemplate>): MutableList<ManualRouteOffsetProfile> {
    val out = mutableListOf<ManualRouteOffsetProfile>()
    for (item in items) {
        val derived = newManualRouteOffsetProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeManualRouteOffsetProfile(item: ManualRouteOffsetProfile): String {
    return globalCarbonReportAdjustmentBucket(item.adjustedVolume())
}
