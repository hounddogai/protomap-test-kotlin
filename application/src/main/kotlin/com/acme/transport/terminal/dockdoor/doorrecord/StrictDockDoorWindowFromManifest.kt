package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.terminal.dwell.dwelldigest.ManualDwellManifest
import com.acme.transport.terminal.dwell.dwelldigest.normalizeManualDwellManifestCode

fun summarizeManualDwellManifestsForWindow(items: MutableList<ManualDwellManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveStrictDockDoorWindowsFromManifest(items: MutableList<ManualDwellManifest>): MutableList<StrictDockDoorWindow> {
    val out = mutableListOf<StrictDockDoorWindow>()
    for (item in items) {
        val derived = newStrictDockDoorWindow(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankStrictDockDoorWindow(item: StrictDockDoorWindow): String = normalizeManualDwellManifestCode(item.code)
