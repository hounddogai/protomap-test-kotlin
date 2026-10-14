package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break summary. */
data class RestBreakSummaryView(
    var codeText: String,
    var name: String,
    var currency: String,
    var attempts: String,
    var windowDaysText: String,
    var sourceText: String,
    var depot: String,
    var statusLabel: String,
)

fun toRestBreakSummaryView(item: RestBreakSummary): RestBreakSummaryView {
    return RestBreakSummaryView(
        codeText = item.code,
        name = truncate(item.name, 80),
        currency = truncate(item.currency, 80),
        attempts = item.attempts.toString(),
        windowDaysText = item.windowDays.toString(),
        sourceText = item.source,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakSummaryViews(items: MutableList<RestBreakSummary>): MutableList<RestBreakSummaryView> {
    val views = mutableListOf<RestBreakSummaryView>()
    for (item in items) {
        views.add(toRestBreakSummaryView(item))
    }
    return views
}

fun sumRestBreakSummaryView(view: RestBreakSummaryView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedAttempts.toDouble() + parsedWindowDays.toDouble()
}
