package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.terminal.crossdock.profile.LegacyCrossDockProfile
import com.acme.transport.terminal.crossdock.profile.cappedCrossDockSnapshotWeight

fun summarizeLegacyCrossDockProfilesForBundle(items: MutableList<LegacyCrossDockProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingTrailerYardBundlesFromProfile(items: MutableList<LegacyCrossDockProfile>): MutableList<RollingTrailerYardBundle> {
    val out = mutableListOf<RollingTrailerYardBundle>()
    for (item in items) {
        val derived = newRollingTrailerYardBundle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankRollingTrailerYardBundle(item: RollingTrailerYardBundle): Double = cappedCrossDockSnapshotWeight(6, true)
