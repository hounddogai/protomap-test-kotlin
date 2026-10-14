package com.acme.transport.linehaul.trailer.claim

/** How screens and exports show a default trailer claim. */
data class DefaultTrailerClaimView(
    var codeText: String,
    var label: String,
    var floorValue: String,
    var title: String,
    var fuelLiters: String,
    var depot: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toDefaultTrailerClaimView(item: DefaultTrailerClaim): DefaultTrailerClaimView {
    return DefaultTrailerClaimView(
        codeText = item.code,
        label = item.label,
        floorValue = "%.2f".format(item.floorValue),
        title = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        depot = item.depot,
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTrailerClaimViews(items: MutableList<DefaultTrailerClaim>): MutableList<DefaultTrailerClaimView> {
    val views = mutableListOf<DefaultTrailerClaimView>()
    for (item in items) {
        views.add(toDefaultTrailerClaimView(item))
    }
    return views
}

fun sumDefaultTrailerClaimView(view: DefaultTrailerClaimView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedFloorValue + parsedFuelLiters + parsedRevision.toDouble()
}
