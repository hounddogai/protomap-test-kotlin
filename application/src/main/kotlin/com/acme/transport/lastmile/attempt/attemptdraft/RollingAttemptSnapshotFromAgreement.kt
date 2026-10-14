package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.lastmile.drop.dropexception.LenientDropAgreement
import com.acme.transport.lastmile.drop.dropexception.lenientDropAgreementWeight

fun summarizeLenientDropAgreementsForSnapshot(items: MutableList<LenientDropAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingAttemptSnapshotsFromAgreement(items: MutableList<LenientDropAgreement>): MutableList<RollingAttemptSnapshot> {
    val out = mutableListOf<RollingAttemptSnapshot>()
    for (item in items) {
        val derived = newRollingAttemptSnapshot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignRollingAttemptSnapshot(item: RollingAttemptSnapshot): Double = lenientDropAgreementWeight(5, true)
