package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy roster template. */
data class LegacyRosterTemplateView(
    var code: String,
    var title: String,
    var priority: String,
    var routeCode: String,
    var maximum: String,
    var discount: String,
    var trailerId: String,
    var graceDays: String,
    var carrierText: String,
    var attempts: String,
    var dock: String,
    var statusLabel: String,
)

fun toLegacyRosterTemplateView(item: LegacyRosterTemplate): LegacyRosterTemplateView {
    return LegacyRosterTemplateView(
        code = item.code,
        title = truncate(item.title, 64),
        priority = item.priority.toString(),
        routeCode = item.routeCode,
        maximum = "%.2f".format(item.maximum),
        discount = "%.2f".format(item.discount),
        trailerId = truncate(item.trailerId, 80),
        graceDays = item.graceDays.toString(),
        carrierText = item.carrier,
        attempts = item.attempts.toString(),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRosterTemplateViews(items: MutableList<LegacyRosterTemplate>): MutableList<LegacyRosterTemplateView> {
    val views = mutableListOf<LegacyRosterTemplateView>()
    for (item in items) {
        views.add(toLegacyRosterTemplateView(item))
    }
    return views
}

fun sumLegacyRosterTemplateView(view: LegacyRosterTemplateView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedPriority.toDouble() + parsedMaximum + parsedDiscount
}
