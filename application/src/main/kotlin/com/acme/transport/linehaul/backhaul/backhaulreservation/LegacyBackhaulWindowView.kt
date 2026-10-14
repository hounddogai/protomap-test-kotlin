package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy backhaul window. */
data class LegacyBackhaulWindowView(
    var code: String,
    var title: String,
    var notesText: String,
    var pinnedText: String,
    var ceilingText: String,
    var minimum: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toLegacyBackhaulWindowView(item: LegacyBackhaulWindow): LegacyBackhaulWindowView {
    return LegacyBackhaulWindowView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 120),
        notesText = item.notes,
        pinnedText = "${item.pinned}",
        ceilingText = "%.2f".format(item.ceiling),
        minimum = "%.2f".format(item.minimum),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyBackhaulWindowViews(items: MutableList<LegacyBackhaulWindow>): MutableList<LegacyBackhaulWindowView> {
    val views = mutableListOf<LegacyBackhaulWindowView>()
    for (item in items) {
        views.add(toLegacyBackhaulWindowView(item))
    }
    return views
}

fun sumLegacyBackhaulWindowView(view: LegacyBackhaulWindowView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedCeiling + parsedMinimum + parsedRetries.toDouble()
}
