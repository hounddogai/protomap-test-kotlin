package com.acme.transport.lastmile.drop.rule

import com.acme.transport.lastmile.locker.lockerclaim.PrimaryLockerDraft
import com.acme.transport.lastmile.locker.lockerclaim.primaryLockerDraftBucket

fun summarizePrimaryLockerDraftsForForecast(items: MutableList<PrimaryLockerDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveGlobalDropForecastsFromDraft(items: MutableList<PrimaryLockerDraft>): MutableList<GlobalDropForecast> {
    val out = mutableListOf<GlobalDropForecast>()
    for (item in items) {
        val derived = newGlobalDropForecast(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceGlobalDropForecast(item: GlobalDropForecast): String = primaryLockerDraftBucket(item.measuredValue())
