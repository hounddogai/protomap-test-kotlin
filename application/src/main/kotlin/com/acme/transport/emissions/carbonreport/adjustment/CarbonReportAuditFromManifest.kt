package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.emissions.emissionfactor.manifest.LegacyEmissionFactorManifest
import com.acme.transport.emissions.emissionfactor.manifest.normalizeLegacyEmissionFactorManifestCode

fun summarizeLegacyEmissionFactorManifestsForAudit(items: MutableList<LegacyEmissionFactorManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveCarbonReportAuditsFromManifest(items: MutableList<LegacyEmissionFactorManifest>): MutableList<CarbonReportAudit> {
    val out = mutableListOf<CarbonReportAudit>()
    for (item in items) {
        val derived = newCarbonReportAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeCarbonReportAudit(item: CarbonReportAudit): String {
    return normalizeLegacyEmissionFactorManifestCode(item.code)
}
