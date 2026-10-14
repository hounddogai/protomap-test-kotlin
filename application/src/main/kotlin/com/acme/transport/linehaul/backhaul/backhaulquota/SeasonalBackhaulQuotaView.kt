package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal backhaul quota. */
data class SeasonalBackhaulQuotaView(
    var code: String,
    var label: String,
    var driverId: String,
    var floorValueText: String,
    var distanceKm: String,
    var vehicleIdText: String,
    var unit: String,
    var statusLabel: String,
)

fun toSeasonalBackhaulQuotaView(item: SeasonalBackhaulQuota): SeasonalBackhaulQuotaView {
    return SeasonalBackhaulQuotaView(
        code = item.code,
        label = item.label,
        driverId = truncate(item.driverId, 40),
        floorValueText = "%.2f".format(item.floorValue),
        distanceKm = "%.2f".format(item.distanceKm),
        vehicleIdText = item.vehicleId,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalBackhaulQuotaViews(items: MutableList<SeasonalBackhaulQuota>): MutableList<SeasonalBackhaulQuotaView> {
    val views = mutableListOf<SeasonalBackhaulQuotaView>()
    for (item in items) {
        views.add(toSeasonalBackhaulQuotaView(item))
    }
    return views
}

fun sumSeasonalBackhaulQuotaView(view: SeasonalBackhaulQuotaView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedFloorValue + parsedDistanceKm
}
