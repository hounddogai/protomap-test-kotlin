package com.acme.transport.crewing.swap.recorditem

/** How screens and exports show a tiered swap checkpoint. */
data class TieredSwapCheckpointView(
    var code: String,
    var name: String,
    var fuelLiters: String,
    var descriptionText: String,
    var expiresAt: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toTieredSwapCheckpointView(item: TieredSwapCheckpoint): TieredSwapCheckpointView {
    return TieredSwapCheckpointView(
        code = item.code,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        descriptionText = item.description,
        expiresAt = item.expiresAt.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSwapCheckpointViews(items: MutableList<TieredSwapCheckpoint>): MutableList<TieredSwapCheckpointView> {
    val views = mutableListOf<TieredSwapCheckpointView>()
    for (item in items) {
        views.add(toTieredSwapCheckpointView(item))
    }
    return views
}

fun sumTieredSwapCheckpointView(view: TieredSwapCheckpointView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedFuelLiters + parsedExpiresAt.toDouble() + parsedDistanceKm
}
