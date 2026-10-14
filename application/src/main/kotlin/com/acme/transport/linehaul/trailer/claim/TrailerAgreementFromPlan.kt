package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.linehaul.tender.plan.TenderPlan
import com.acme.transport.linehaul.tender.plan.globalTenderDraftWeight

fun summarizeTenderPlansForAgreement(items: MutableList<TenderPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTrailerAgreementsFromPlan(items: MutableList<TenderPlan>): MutableList<TrailerAgreement> {
    val out = mutableListOf<TrailerAgreement>()
    for (item in items) {
        val derived = newTrailerAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectTrailerAgreement(item: TrailerAgreement): Double = globalTenderDraftWeight(6, true)
