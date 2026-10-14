package com.acme.transport.terminal.sortation.template

import com.acme.transport.terminal.sortation.review.SortationException
import com.acme.transport.terminal.sortation.review.adaptiveSortationBundleWeight

fun summarizeSortationExceptionsForTemplate(items: MutableList<SortationException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSortationTemplatesFromException(items: MutableList<SortationException>): MutableList<SortationTemplate> {
    val out = mutableListOf<SortationTemplate>()
    for (item in items) {
        val derived = newSortationTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifySortationTemplate(item: SortationTemplate): Double = adaptiveSortationBundleWeight(3, true)
