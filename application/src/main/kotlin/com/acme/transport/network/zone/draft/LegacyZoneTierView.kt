package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy zone tier. */
data class LegacyZoneTierView(
    var code: String,
    var name: String,
    var archived: String,
    var weightText: String,
    var retriesText: String,
    var endsAt: String,
    var ratioText: String,
    var unitText: String,
    var depot: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toLegacyZoneTierView(item: LegacyZoneTier): LegacyZoneTierView {
    return LegacyZoneTierView(
        code = item.code,
        name = item.name,
        archived = "${item.archived}",
        weightText = "%.2f".format(item.weight),
        retriesText = item.retries.toString(),
        endsAt = item.endsAt.toString(),
        ratioText = "%.2f".format(item.ratio),
        unitText = item.unit,
        depot = truncate(item.depot, 64),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyZoneTierViews(items: MutableList<LegacyZoneTier>): MutableList<LegacyZoneTierView> {
    val views = mutableListOf<LegacyZoneTierView>()
    for (item in items) {
        views.add(toLegacyZoneTierView(item))
    }
    return views
}

fun sumLegacyZoneTierView(view: LegacyZoneTierView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedWeight + parsedRetries.toDouble() + parsedEndsAt.toDouble()
}
