package com.acme.transport.crewing.restbreak.claim

/** How screens and exports show a strict rest break claim. */
data class StrictRestBreakClaimView(
    var code: String,
    var titleText: String,
    var trailerId: String,
    var maximumText: String,
    var expiresAt: String,
    var discountText: String,
    var driverIdText: String,
    var depotText: String,
    var unitText: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toStrictRestBreakClaimView(item: StrictRestBreakClaim): StrictRestBreakClaimView {
    return StrictRestBreakClaimView(
        code = item.code,
        titleText = item.title,
        trailerId = item.trailerId,
        maximumText = "%.2f".format(item.maximum),
        expiresAt = item.expiresAt.toString(),
        discountText = "%.2f".format(item.discount),
        driverIdText = item.driverId,
        depotText = item.depot,
        unitText = item.unit,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRestBreakClaimViews(items: MutableList<StrictRestBreakClaim>): MutableList<StrictRestBreakClaimView> {
    val views = mutableListOf<StrictRestBreakClaimView>()
    for (item in items) {
        views.add(toStrictRestBreakClaimView(item))
    }
    return views
}

fun sumStrictRestBreakClaimView(view: StrictRestBreakClaimView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedMaximum + parsedExpiresAt.toDouble() + parsedDiscount
}
