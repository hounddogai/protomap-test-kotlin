package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation rule. */
data class SortationRuleView(
    var code: String,
    var title: String,
    var etaMinutes: String,
    var windowDays: String,
    var source: String,
    var statusLabel: String,
)

fun toSortationRuleView(item: SortationRule): SortationRuleView {
    return SortationRuleView(
        code = truncate(item.code, 120),
        title = item.title,
        etaMinutes = item.etaMinutes.toString(),
        windowDays = item.windowDays.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationRuleViews(items: MutableList<SortationRule>): MutableList<SortationRuleView> {
    val views = mutableListOf<SortationRuleView>()
    for (item in items) {
        views.add(toSortationRuleView(item))
    }
    return views
}

fun sumSortationRuleView(view: SortationRuleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedEtaMinutes.toDouble() + parsedWindowDays.toDouble()
}
