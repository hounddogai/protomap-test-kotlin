package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary lease return profile. */
data class PrimaryLeaseReturnProfileView(
    var codeText: String,
    var nameText: String,
    var attemptsText: String,
    var quantityText: String,
    var carrierText: String,
    var startsAt: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toPrimaryLeaseReturnProfileView(item: PrimaryLeaseReturnProfile): PrimaryLeaseReturnProfileView {
    return PrimaryLeaseReturnProfileView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        attemptsText = item.attempts.toString(),
        quantityText = item.quantity.toString(),
        carrierText = item.carrier,
        startsAt = item.startsAt.toString(),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryLeaseReturnProfileViews(items: MutableList<PrimaryLeaseReturnProfile>): MutableList<PrimaryLeaseReturnProfileView> {
    val views = mutableListOf<PrimaryLeaseReturnProfileView>()
    for (item in items) {
        views.add(toPrimaryLeaseReturnProfileView(item))
    }
    return views
}

fun sumPrimaryLeaseReturnProfileView(view: PrimaryLeaseReturnProfileView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedAttempts.toDouble() + parsedQuantity.toDouble() + parsedStartsAt.toDouble()
}
