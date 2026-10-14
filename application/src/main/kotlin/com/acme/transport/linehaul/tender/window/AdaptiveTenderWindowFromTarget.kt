package com.acme.transport.linehaul.tender.window

import com.acme.transport.linehaul.relay.relaytarget.SeasonalRelayTarget
import com.acme.transport.linehaul.relay.relaytarget.seasonalRelayTargetWeight

fun summarizeSeasonalRelayTargetsForWindow(items: MutableList<SeasonalRelayTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveAdaptiveTenderWindowsFromTarget(items: MutableList<SeasonalRelayTarget>): MutableList<AdaptiveTenderWindow> {
    val out = mutableListOf<AdaptiveTenderWindow>()
    for (item in items) {
        val derived = newAdaptiveTenderWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateAdaptiveTenderWindow(item: AdaptiveTenderWindow): Double = seasonalRelayTargetWeight(4, true)
