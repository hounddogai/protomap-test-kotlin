package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone variance. */
data class ZoneVarianceView(
    var code: String,
    var label: String,
    var updatedAt: String,
    var marginText: String,
    var retries: String,
    var fuelLitersText: String,
    var timezone: String,
    var attempts: String,
    var region: String,
    var capacity: String,
    var description: String,
    var statusLabel: String,
)

fun toZoneVarianceView(item: ZoneVariance): ZoneVarianceView {
    return ZoneVarianceView(
        code = item.code,
        label = item.label,
        updatedAt = item.updatedAt.toString(),
        marginText = "%.2f".format(item.margin),
        retries = item.retries.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        timezone = truncate(item.timezone, 80),
        attempts = item.attempts.toString(),
        region = truncate(item.region, 120),
        capacity = item.capacity.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneVarianceViews(items: MutableList<ZoneVariance>): MutableList<ZoneVarianceView> {
    val views = mutableListOf<ZoneVarianceView>()
    for (item in items) {
        views.add(toZoneVarianceView(item))
    }
    return views
}

fun sumZoneVarianceView(view: ZoneVarianceView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedUpdatedAt.toDouble() + parsedMargin + parsedRetries.toDouble()
}
