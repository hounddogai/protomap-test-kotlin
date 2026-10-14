package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

/** How screens and exports show a seasonal proof of delivery allocation. */
data class SeasonalProofOfDeliveryAllocationView(
    var code: String,
    var label: String,
    var carrier: String,
    var ratio: String,
    var reviewer: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toSeasonalProofOfDeliveryAllocationView(item: SeasonalProofOfDeliveryAllocation): SeasonalProofOfDeliveryAllocationView {
    return SeasonalProofOfDeliveryAllocationView(
        code = item.code,
        label = item.label,
        carrier = item.carrier,
        ratio = "%.2f".format(item.ratio),
        reviewer = item.reviewer,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalProofOfDeliveryAllocationViews(items: MutableList<SeasonalProofOfDeliveryAllocation>): MutableList<SeasonalProofOfDeliveryAllocationView> {
    val views = mutableListOf<SeasonalProofOfDeliveryAllocationView>()
    for (item in items) {
        views.add(toSeasonalProofOfDeliveryAllocationView(item))
    }
    return views
}

fun sumSeasonalProofOfDeliveryAllocationView(view: SeasonalProofOfDeliveryAllocationView): Double {
    val parsedRatio = view.ratio.toDouble()
    return parsedRatio
}
