package com.acme.transport.crewing.availability.weightedavailabilityreservation

/** How screens and exports show a tiered availability reservation. */
data class TieredAvailabilityReservationView(
    var codeText: String,
    var titleText: String,
    var reviewerText: String,
    var ratio: String,
    var endsAtText: String,
    var ceiling: String,
    var retries: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toTieredAvailabilityReservationView(item: TieredAvailabilityReservation): TieredAvailabilityReservationView {
    return TieredAvailabilityReservationView(
        codeText = item.code,
        titleText = item.title,
        reviewerText = item.reviewer,
        ratio = "%.2f".format(item.ratio),
        endsAtText = item.endsAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        retries = item.retries.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAvailabilityReservationViews(items: MutableList<TieredAvailabilityReservation>): MutableList<TieredAvailabilityReservationView> {
    val views = mutableListOf<TieredAvailabilityReservationView>()
    for (item in items) {
        views.add(toTieredAvailabilityReservationView(item))
    }
    return views
}

fun sumTieredAvailabilityReservationView(view: TieredAvailabilityReservationView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedRatio + parsedEndsAt.toDouble() + parsedCeiling
}
