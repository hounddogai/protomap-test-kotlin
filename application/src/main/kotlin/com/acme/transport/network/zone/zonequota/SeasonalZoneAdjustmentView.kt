package com.acme.transport.network.zone.zonequota

/** How screens and exports show a seasonal zone adjustment. */
data class SeasonalZoneAdjustmentView(
    var code: String,
    var nameText: String,
    var driverId: String,
    var phone: String,
    var attempts: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toSeasonalZoneAdjustmentView(item: SeasonalZoneAdjustment): SeasonalZoneAdjustmentView {
    return SeasonalZoneAdjustmentView(
        code = item.code,
        nameText = item.name,
        driverId = item.driverId,
        phone = item.phone,
        attempts = item.attempts.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalZoneAdjustmentViews(items: MutableList<SeasonalZoneAdjustment>): MutableList<SeasonalZoneAdjustmentView> {
    val views = mutableListOf<SeasonalZoneAdjustmentView>()
    for (item in items) {
        views.add(toSeasonalZoneAdjustmentView(item))
    }
    return views
}

fun sumSeasonalZoneAdjustmentView(view: SeasonalZoneAdjustmentView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedAttempts.toDouble() + parsedCeiling
}
