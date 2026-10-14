package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal camera event quota. */
data class SeasonalCameraEventQuotaView(
    var code: String,
    var name: String,
    var version: String,
    var pallets: String,
    var notes: String,
    var carrier: String,
    var localeText: String,
    var driverIdText: String,
    var score: String,
    var regionText: String,
    var statusLabel: String,
)

fun toSeasonalCameraEventQuotaView(item: SeasonalCameraEventQuota): SeasonalCameraEventQuotaView {
    return SeasonalCameraEventQuotaView(
        code = item.code,
        name = truncate(item.name, 40),
        version = item.version.toString(),
        pallets = item.pallets.toString(),
        notes = truncate(item.notes, 64),
        carrier = item.carrier,
        localeText = item.locale,
        driverIdText = item.driverId,
        score = "%.2f".format(item.score),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCameraEventQuotaViews(items: MutableList<SeasonalCameraEventQuota>): MutableList<SeasonalCameraEventQuotaView> {
    val views = mutableListOf<SeasonalCameraEventQuotaView>()
    for (item in items) {
        views.add(toSeasonalCameraEventQuotaView(item))
    }
    return views
}

fun sumSeasonalCameraEventQuotaView(view: SeasonalCameraEventQuotaView): Double {
    val parsedVersion = view.version.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedScore = view.score.toDouble()
    return parsedVersion.toDouble() + parsedPallets.toDouble() + parsedScore
}
