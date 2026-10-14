package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict coaching entry. */
data class StrictCoachingEntryView(
    var code: String,
    var name: String,
    var retries: String,
    var minimumText: String,
    var windowDaysText: String,
    var externalRef: String,
    var quantity: String,
    var countryText: String,
    var dockText: String,
    var statusLabel: String,
)

fun toStrictCoachingEntryView(item: StrictCoachingEntry): StrictCoachingEntryView {
    return StrictCoachingEntryView(
        code = truncate(item.code, 64),
        name = item.name,
        retries = item.retries.toString(),
        minimumText = "%.2f".format(item.minimum),
        windowDaysText = item.windowDays.toString(),
        externalRef = item.externalRef,
        quantity = item.quantity.toString(),
        countryText = item.country,
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCoachingEntryViews(items: MutableList<StrictCoachingEntry>): MutableList<StrictCoachingEntryView> {
    val views = mutableListOf<StrictCoachingEntryView>()
    for (item in items) {
        views.add(toStrictCoachingEntryView(item))
    }
    return views
}

fun sumStrictCoachingEntryView(view: StrictCoachingEntryView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedRetries.toDouble() + parsedMinimum + parsedWindowDays.toDouble()
}
