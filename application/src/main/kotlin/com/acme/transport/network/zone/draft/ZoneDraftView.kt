package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone draft. */
data class ZoneDraftView(
    var code: String,
    var label: String,
    var capacityText: String,
    var notes: String,
    var endsAtText: String,
    var graceDaysText: String,
    var title: String,
    var statusLabel: String,
)

fun toZoneDraftView(item: ZoneDraft): ZoneDraftView {
    return ZoneDraftView(
        code = item.code,
        label = truncate(item.label, 120),
        capacityText = item.capacity.toString(),
        notes = item.notes,
        endsAtText = item.endsAt.toString(),
        graceDaysText = item.graceDays.toString(),
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneDraftViews(items: MutableList<ZoneDraft>): MutableList<ZoneDraftView> {
    val views = mutableListOf<ZoneDraftView>()
    for (item in items) {
        views.add(toZoneDraftView(item))
    }
    return views
}

fun sumZoneDraftView(view: ZoneDraftView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedCapacity.toDouble() + parsedEndsAt.toDouble() + parsedGraceDays.toDouble()
}
