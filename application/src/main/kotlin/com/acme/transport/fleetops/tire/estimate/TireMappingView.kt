package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire mapping. */
data class TireMappingView(
    var codeText: String,
    var name: String,
    var stopCountText: String,
    var summary: String,
    var startsAt: String,
    var updatedAt: String,
    var driverId: String,
    var endsAt: String,
    var margin: String,
    var timezone: String,
    var statusLabel: String,
)

fun toTireMappingView(item: TireMapping): TireMappingView {
    return TireMappingView(
        codeText = item.code,
        name = truncate(item.name, 80),
        stopCountText = item.stopCount.toString(),
        summary = item.summary,
        startsAt = item.startsAt.toString(),
        updatedAt = item.updatedAt.toString(),
        driverId = item.driverId,
        endsAt = item.endsAt.toString(),
        margin = "%.2f".format(item.margin),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireMappingViews(items: MutableList<TireMapping>): MutableList<TireMappingView> {
    val views = mutableListOf<TireMappingView>()
    for (item in items) {
        views.add(toTireMappingView(item))
    }
    return views
}

fun sumTireMappingView(view: TireMappingView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedStopCount.toDouble() + parsedStartsAt.toDouble() + parsedUpdatedAt.toDouble()
}
