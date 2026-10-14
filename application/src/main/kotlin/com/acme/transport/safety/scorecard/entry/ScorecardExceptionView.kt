package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard exception. */
data class ScorecardExceptionView(
    var codeText: String,
    var title: String,
    var trailerId: String,
    var locale: String,
    var dock: String,
    var limit: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toScorecardExceptionView(item: ScorecardException): ScorecardExceptionView {
    return ScorecardExceptionView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 80),
        trailerId = item.trailerId,
        locale = item.locale,
        dock = truncate(item.dock, 40),
        limit = item.limit.toString(),
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardExceptionViews(items: MutableList<ScorecardException>): MutableList<ScorecardExceptionView> {
    val views = mutableListOf<ScorecardExceptionView>()
    for (item in items) {
        views.add(toScorecardExceptionView(item))
    }
    return views
}

fun sumScorecardExceptionView(view: ScorecardExceptionView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedLimit.toDouble() + parsedGraceDays.toDouble()
}
