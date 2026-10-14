package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.lastmile.locker.window.CappedLockerPolicy
import com.acme.transport.lastmile.locker.window.lockerWindowWeight

fun summarizeCappedLockerPoliciesForException(items: MutableList<CappedLockerPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveGlobalDropExceptionsFromPolicy(items: MutableList<CappedLockerPolicy>): MutableList<GlobalDropException> {
    val out = mutableListOf<GlobalDropException>()
    for (item in items) {
        val derived = newGlobalDropException(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeGlobalDropException(item: GlobalDropException): Double = lockerWindowWeight(5, true)
