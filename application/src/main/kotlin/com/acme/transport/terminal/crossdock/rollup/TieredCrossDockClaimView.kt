package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered cross dock claim. */
data class TieredCrossDockClaimView(
    var codeText: String,
    var name: String,
    var summary: String,
    var quantity: String,
    var odometer: String,
    var pinned: String,
    var revisionText: String,
    var driverId: String,
    var category: String,
    var score: String,
    var statusLabel: String,
)

fun toTieredCrossDockClaimView(item: TieredCrossDockClaim): TieredCrossDockClaimView {
    return TieredCrossDockClaimView(
        codeText = item.code,
        name = item.name,
        summary = truncate(item.summary, 80),
        quantity = item.quantity.toString(),
        odometer = item.odometer.toString(),
        pinned = "${item.pinned}",
        revisionText = item.revision.toString(),
        driverId = truncate(item.driverId, 40),
        category = truncate(item.category, 64),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCrossDockClaimViews(items: MutableList<TieredCrossDockClaim>): MutableList<TieredCrossDockClaimView> {
    val views = mutableListOf<TieredCrossDockClaimView>()
    for (item in items) {
        views.add(toTieredCrossDockClaimView(item))
    }
    return views
}

fun sumTieredCrossDockClaimView(view: TieredCrossDockClaimView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedRevision = view.revisionText.toInt()
    return parsedQuantity.toDouble() + parsedOdometer.toDouble() + parsedRevision.toDouble()
}
