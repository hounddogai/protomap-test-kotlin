package com.acme.transport.emissions.routeoffset.estimate

/** How screens and exports show a seasonal route offset claim. */
data class SeasonalRouteOffsetClaimView(
    var codeText: String,
    var labelText: String,
    var distanceKmText: String,
    var depotText: String,
    var floorValue: String,
    var createdAt: String,
    var stopCount: String,
    var capacityText: String,
    var name: String,
    var contactEmail: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toSeasonalRouteOffsetClaimView(item: SeasonalRouteOffsetClaim): SeasonalRouteOffsetClaimView {
    return SeasonalRouteOffsetClaimView(
        codeText = item.code,
        labelText = item.label,
        distanceKmText = "%.2f".format(item.distanceKm),
        depotText = item.depot,
        floorValue = "%.2f".format(item.floorValue),
        createdAt = item.createdAt.toString(),
        stopCount = item.stopCount.toString(),
        capacityText = item.capacity.toString(),
        name = item.name,
        contactEmail = item.contactEmail,
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRouteOffsetClaimViews(items: MutableList<SeasonalRouteOffsetClaim>): MutableList<SeasonalRouteOffsetClaimView> {
    val views = mutableListOf<SeasonalRouteOffsetClaimView>()
    for (item in items) {
        views.add(toSeasonalRouteOffsetClaimView(item))
    }
    return views
}

fun sumSeasonalRouteOffsetClaimView(view: SeasonalRouteOffsetClaimView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedDistanceKm + parsedFloorValue + parsedCreatedAt.toDouble()
}
