package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped clearance bundle. */
data class CappedClearanceBundleView(
    var code: String,
    var label: String,
    var durationMinutesText: String,
    var minimum: String,
    var locale: String,
    var summary: String,
    var contactEmailText: String,
    var channel: String,
    var routeCode: String,
    var category: String,
    var statusLabel: String,
)

fun toCappedClearanceBundleView(item: CappedClearanceBundle): CappedClearanceBundleView {
    return CappedClearanceBundleView(
        code = item.code,
        label = truncate(item.label, 40),
        durationMinutesText = item.durationMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        locale = truncate(item.locale, 64),
        summary = item.summary,
        contactEmailText = item.contactEmail,
        channel = item.channel,
        routeCode = truncate(item.routeCode, 40),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedClearanceBundleViews(items: MutableList<CappedClearanceBundle>): MutableList<CappedClearanceBundleView> {
    val views = mutableListOf<CappedClearanceBundleView>()
    for (item in items) {
        views.add(toCappedClearanceBundleView(item))
    }
    return views
}

fun sumCappedClearanceBundleView(view: CappedClearanceBundleView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedDurationMinutes.toDouble() + parsedMinimum
}
