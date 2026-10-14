package com.acme.transport.safety.incident.variance

/** How screens and exports show a regional incident claim. */
data class RegionalIncidentClaimView(
    var code: String,
    var nameText: String,
    var updatedAt: String,
    var fuelLiters: String,
    var region: String,
    var trailerId: String,
    var weightText: String,
    var checksum: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toRegionalIncidentClaimView(item: RegionalIncidentClaim): RegionalIncidentClaimView {
    return RegionalIncidentClaimView(
        code = item.code,
        nameText = item.name,
        updatedAt = item.updatedAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        region = item.region,
        trailerId = item.trailerId,
        weightText = "%.2f".format(item.weight),
        checksum = item.checksum,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalIncidentClaimViews(items: MutableList<RegionalIncidentClaim>): MutableList<RegionalIncidentClaimView> {
    val views = mutableListOf<RegionalIncidentClaimView>()
    for (item in items) {
        views.add(toRegionalIncidentClaimView(item))
    }
    return views
}

fun sumRegionalIncidentClaimView(view: RegionalIncidentClaimView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedFuelLiters + parsedWeight
}
