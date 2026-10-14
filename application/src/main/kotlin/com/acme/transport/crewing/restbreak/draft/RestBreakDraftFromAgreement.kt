package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.crewing.qualification.digest.DefaultQualificationAgreement
import com.acme.transport.crewing.qualification.digest.normalizeQualificationDigestCode

fun summarizeDefaultQualificationAgreementsForDraft(items: MutableList<DefaultQualificationAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRestBreakDraftsFromAgreement(items: MutableList<DefaultQualificationAgreement>): MutableList<RestBreakDraft> {
    val out = mutableListOf<RestBreakDraft>()
    for (item in items) {
        val derived = newRestBreakDraft(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreRestBreakDraft(item: RestBreakDraft): String = normalizeQualificationDigestCode(item.code)
