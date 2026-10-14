package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary defect claim. */
data class PrimaryDefectClaimView(
    var code: String,
    var titleText: String,
    var archived: String,
    var attemptsText: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toPrimaryDefectClaimView(item: PrimaryDefectClaim): PrimaryDefectClaimView {
    return PrimaryDefectClaimView(
        code = item.code,
        titleText = truncate(item.title, 64),
        archived = "${item.archived}",
        attemptsText = item.attempts.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDefectClaimViews(items: MutableList<PrimaryDefectClaim>): MutableList<PrimaryDefectClaimView> {
    val views = mutableListOf<PrimaryDefectClaimView>()
    for (item in items) {
        views.add(toPrimaryDefectClaimView(item))
    }
    return views
}

fun sumPrimaryDefectClaimView(view: PrimaryDefectClaimView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedAttempts.toDouble() + parsedFuelLiters
}
