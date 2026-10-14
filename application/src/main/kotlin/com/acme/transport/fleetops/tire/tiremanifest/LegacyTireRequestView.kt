package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy tire request. */
data class LegacyTireRequestView(
    var code: String,
    var name: String,
    var pinned: String,
    var timezoneText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toLegacyTireRequestView(item: LegacyTireRequest): LegacyTireRequestView {
    return LegacyTireRequestView(
        code = truncate(item.code, 120),
        name = item.name,
        pinned = "${item.pinned}",
        timezoneText = item.timezone,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTireRequestViews(items: MutableList<LegacyTireRequest>): MutableList<LegacyTireRequestView> {
    val views = mutableListOf<LegacyTireRequestView>()
    for (item in items) {
        views.add(toLegacyTireRequestView(item))
    }
    return views
}

fun sumLegacyTireRequestView(view: LegacyTireRequestView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}
