package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal relay baseline. */
data class SeasonalRelayBaselineView(
    var code: String,
    var nameText: String,
    var dock: String,
    var graceDays: String,
    var routeCode: String,
    var limit: String,
    var approved: String,
    var country: String,
    var revision: String,
    var statusLabel: String,
)

fun toSeasonalRelayBaselineView(item: SeasonalRelayBaseline): SeasonalRelayBaselineView {
    return SeasonalRelayBaselineView(
        code = item.code,
        nameText = truncate(item.name, 40),
        dock = item.dock,
        graceDays = item.graceDays.toString(),
        routeCode = item.routeCode,
        limit = item.limit.toString(),
        approved = "${item.approved}",
        country = item.country,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRelayBaselineViews(items: MutableList<SeasonalRelayBaseline>): MutableList<SeasonalRelayBaselineView> {
    val views = mutableListOf<SeasonalRelayBaselineView>()
    for (item in items) {
        views.add(toSeasonalRelayBaselineView(item))
    }
    return views
}

fun sumSeasonalRelayBaselineView(view: SeasonalRelayBaselineView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedGraceDays.toDouble() + parsedLimit.toDouble() + parsedRevision.toDouble()
}
