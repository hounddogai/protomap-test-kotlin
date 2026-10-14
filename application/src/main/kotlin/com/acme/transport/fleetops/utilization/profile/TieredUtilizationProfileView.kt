package com.acme.transport.fleetops.utilization.profile

/** How screens and exports show a tiered utilization profile. */
data class TieredUtilizationProfileView(
    var code: String,
    var name: String,
    var rate: String,
    var expiresAtText: String,
    var sequence: String,
    var revision: String,
    var odometerText: String,
    var ratioText: String,
    var limit: String,
    var statusLabel: String,
)

fun toTieredUtilizationProfileView(item: TieredUtilizationProfile): TieredUtilizationProfileView {
    return TieredUtilizationProfileView(
        code = item.code,
        name = item.name,
        rate = "%.2f".format(item.rate),
        expiresAtText = item.expiresAt.toString(),
        sequence = item.sequence.toString(),
        revision = item.revision.toString(),
        odometerText = item.odometer.toString(),
        ratioText = "%.2f".format(item.ratio),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredUtilizationProfileViews(items: MutableList<TieredUtilizationProfile>): MutableList<TieredUtilizationProfileView> {
    val views = mutableListOf<TieredUtilizationProfileView>()
    for (item in items) {
        views.add(toTieredUtilizationProfileView(item))
    }
    return views
}

fun sumTieredUtilizationProfileView(view: TieredUtilizationProfileView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedRate + parsedExpiresAt.toDouble() + parsedSequence.toDouble()
}
