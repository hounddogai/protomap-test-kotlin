package com.acme.transport.fleetops.tire.quota

/** How screens and exports show a seasonal tire quota. */
data class SeasonalTireQuotaView(
    var code: String,
    var name: String,
    var floorValue: String,
    var etaMinutes: String,
    var expiresAtText: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toSeasonalTireQuotaView(item: SeasonalTireQuota): SeasonalTireQuotaView {
    return SeasonalTireQuotaView(
        code = item.code,
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        etaMinutes = item.etaMinutes.toString(),
        expiresAtText = item.expiresAt.toString(),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTireQuotaViews(items: MutableList<SeasonalTireQuota>): MutableList<SeasonalTireQuotaView> {
    val views = mutableListOf<SeasonalTireQuotaView>()
    for (item in items) {
        views.add(toSeasonalTireQuotaView(item))
    }
    return views
}

fun sumSeasonalTireQuotaView(view: SeasonalTireQuotaView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedFloorValue + parsedEtaMinutes.toDouble() + parsedExpiresAt.toDouble()
}
