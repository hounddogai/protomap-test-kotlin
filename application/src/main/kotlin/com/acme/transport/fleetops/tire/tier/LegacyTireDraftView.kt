package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy tire draft. */
data class LegacyTireDraftView(
    var code: String,
    var title: String,
    var summary: String,
    var priority: String,
    var source: String,
    var statusLabel: String,
)

fun toLegacyTireDraftView(item: LegacyTireDraft): LegacyTireDraftView {
    return LegacyTireDraftView(
        code = truncate(item.code, 120),
        title = item.title,
        summary = item.summary,
        priority = item.priority.toString(),
        source = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTireDraftViews(items: MutableList<LegacyTireDraft>): MutableList<LegacyTireDraftView> {
    val views = mutableListOf<LegacyTireDraftView>()
    for (item in items) {
        views.add(toLegacyTireDraftView(item))
    }
    return views
}

fun sumLegacyTireDraftView(view: LegacyTireDraftView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
