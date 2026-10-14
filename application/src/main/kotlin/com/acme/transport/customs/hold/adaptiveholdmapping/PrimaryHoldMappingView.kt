package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary hold mapping. */
data class PrimaryHoldMappingView(
    var codeText: String,
    var labelText: String,
    var ownerId: String,
    var country: String,
    var distanceKmText: String,
    var attempts: String,
    var discountText: String,
    var archivedText: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toPrimaryHoldMappingView(item: PrimaryHoldMapping): PrimaryHoldMappingView {
    return PrimaryHoldMappingView(
        codeText = item.code,
        labelText = truncate(item.label, 120),
        ownerId = item.ownerId,
        country = item.country,
        distanceKmText = "%.2f".format(item.distanceKm),
        attempts = item.attempts.toString(),
        discountText = "%.2f".format(item.discount),
        archivedText = "${item.archived}",
        reviewerText = truncate(item.reviewer, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryHoldMappingViews(items: MutableList<PrimaryHoldMapping>): MutableList<PrimaryHoldMappingView> {
    val views = mutableListOf<PrimaryHoldMappingView>()
    for (item in items) {
        views.add(toPrimaryHoldMappingView(item))
    }
    return views
}

fun sumPrimaryHoldMappingView(view: PrimaryHoldMappingView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedDistanceKm + parsedAttempts.toDouble() + parsedDiscount
}
