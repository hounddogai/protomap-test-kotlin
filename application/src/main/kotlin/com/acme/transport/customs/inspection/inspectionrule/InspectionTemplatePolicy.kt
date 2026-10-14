package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun inspectionTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun mergeInspectionTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 37514.0, in order. */
fun inspectionTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37514.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a inspection template score falls in. */
fun classifyInspectionTemplate(score: Double): String {
    if (score < 48.5) {
        return "watch"
    }
    if (score < 56.8) {
        return "high"
    }
    if (score < 66.8) {
        return "elevated"
    }
    if (score < 99.3) {
        return "ok"
    }
    return "minimal"
}
