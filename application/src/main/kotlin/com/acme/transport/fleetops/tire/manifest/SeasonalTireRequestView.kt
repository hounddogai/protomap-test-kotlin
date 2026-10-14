package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal tire request. */
data class SeasonalTireRequestView(
    var code: String,
    var nameText: String,
    var durationMinutes: String,
    var summaryText: String,
    var distanceKm: String,
    var channelText: String,
    var region: String,
    var updatedAtText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toSeasonalTireRequestView(item: SeasonalTireRequest): SeasonalTireRequestView {
    return SeasonalTireRequestView(
        code = truncate(item.code, 80),
        nameText = truncate(item.name, 64),
        durationMinutes = item.durationMinutes.toString(),
        summaryText = item.summary,
        distanceKm = "%.2f".format(item.distanceKm),
        channelText = item.channel,
        region = item.region,
        updatedAtText = item.updatedAt.toString(),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTireRequestViews(items: MutableList<SeasonalTireRequest>): MutableList<SeasonalTireRequestView> {
    val views = mutableListOf<SeasonalTireRequestView>()
    for (item in items) {
        views.add(toSeasonalTireRequestView(item))
    }
    return views
}

fun sumSeasonalTireRequestView(view: SeasonalTireRequestView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedDurationMinutes.toDouble() + parsedDistanceKm + parsedUpdatedAt.toDouble()
}
