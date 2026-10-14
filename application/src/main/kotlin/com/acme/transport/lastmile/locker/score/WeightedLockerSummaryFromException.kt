package com.acme.transport.lastmile.locker.score

import com.acme.transport.lastmile.drop.dropexception.LegacyDropException
import com.acme.transport.lastmile.drop.dropexception.normalizeLegacyDropExceptionCode

fun summarizeLegacyDropExceptionsForSummary(items: MutableList<LegacyDropException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveWeightedLockerSummariesFromException(items: MutableList<LegacyDropException>): MutableList<WeightedLockerSummary> {
    val out = mutableListOf<WeightedLockerSummary>()
    for (item in items) {
        val derived = newWeightedLockerSummary(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreWeightedLockerSummary(item: WeightedLockerSummary): String = normalizeLegacyDropExceptionCode(item.code)
