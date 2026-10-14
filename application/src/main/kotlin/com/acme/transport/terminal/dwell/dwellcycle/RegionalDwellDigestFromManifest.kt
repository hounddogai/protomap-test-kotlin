package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.terminal.dwell.dwelldigest.ManualDwellManifest
import com.acme.transport.terminal.dwell.dwelldigest.normalizeManualDwellManifestCode

fun summarizeManualDwellManifestsForDigest(items: MutableList<ManualDwellManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRegionalDwellDigestsFromManifest(items: MutableList<ManualDwellManifest>): MutableList<RegionalDwellDigest> {
    val out = mutableListOf<RegionalDwellDigest>()
    for (item in items) {
        val derived = newRegionalDwellDigest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectRegionalDwellDigest(item: RegionalDwellDigest): String = normalizeManualDwellManifestCode(item.code)
