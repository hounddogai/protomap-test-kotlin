package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.lastmile.proofofdelivery.ledger.FallbackProofOfDeliveryLedger
import com.acme.transport.lastmile.proofofdelivery.ledger.normalizeFallbackProofOfDeliveryLedgerCode

fun summarizeFallbackProofOfDeliveryLedgersForAgreement(items: MutableList<FallbackProofOfDeliveryLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDropAgreementsFromLedger(items: MutableList<FallbackProofOfDeliveryLedger>): MutableList<DropAgreement> {
    val out = mutableListOf<DropAgreement>()
    for (item in items) {
        val derived = newDropAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyDropAgreement(item: DropAgreement): String = normalizeFallbackProofOfDeliveryLedgerCode(item.code)
