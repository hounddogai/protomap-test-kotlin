package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal camera event entry. */
data class SeasonalCameraEventEntryView(
    var code: String,
    var nameText: String,
    var ownerId: String,
    var endsAt: String,
    var externalRef: String,
    var summary: String,
    var statusLabel: String,
)

fun toSeasonalCameraEventEntryView(item: SeasonalCameraEventEntry): SeasonalCameraEventEntryView {
    return SeasonalCameraEventEntryView(
        code = item.code,
        nameText = item.name,
        ownerId = item.ownerId,
        endsAt = item.endsAt.toString(),
        externalRef = truncate(item.externalRef, 64),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCameraEventEntryViews(items: MutableList<SeasonalCameraEventEntry>): MutableList<SeasonalCameraEventEntryView> {
    val views = mutableListOf<SeasonalCameraEventEntryView>()
    for (item in items) {
        views.add(toSeasonalCameraEventEntryView(item))
    }
    return views
}

fun sumSeasonalCameraEventEntryView(view: SeasonalCameraEventEntryView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEndsAt.toDouble()
}
