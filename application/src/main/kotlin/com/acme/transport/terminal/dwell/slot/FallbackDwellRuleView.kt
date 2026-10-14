package com.acme.transport.terminal.dwell.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback dwell rule. */
data class FallbackDwellRuleView(
    var code: String,
    var title: String,
    var regionText: String,
    var etaMinutes: String,
    var summary: String,
    var pinned: String,
    var statusLabel: String,
)

fun toFallbackDwellRuleView(item: FallbackDwellRule): FallbackDwellRuleView {
    return FallbackDwellRuleView(
        code = item.code,
        title = item.title,
        regionText = truncate(item.region, 80),
        etaMinutes = item.etaMinutes.toString(),
        summary = item.summary,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDwellRuleViews(items: MutableList<FallbackDwellRule>): MutableList<FallbackDwellRuleView> {
    val views = mutableListOf<FallbackDwellRuleView>()
    for (item in items) {
        views.add(toFallbackDwellRuleView(item))
    }
    return views
}

fun sumFallbackDwellRuleView(view: FallbackDwellRuleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
