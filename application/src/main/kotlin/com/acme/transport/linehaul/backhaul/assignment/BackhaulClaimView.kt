package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul claim. */
data class BackhaulClaimView(
    var code: String,
    var nameText: String,
    var vehicleIdText: String,
    var description: String,
    var retries: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toBackhaulClaimView(item: BackhaulClaim): BackhaulClaimView {
    return BackhaulClaimView(
        code = item.code,
        nameText = truncate(item.name, 120),
        vehicleIdText = item.vehicleId,
        description = truncate(item.description, 80),
        retries = item.retries.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulClaimViews(items: MutableList<BackhaulClaim>): MutableList<BackhaulClaimView> {
    val views = mutableListOf<BackhaulClaimView>()
    for (item in items) {
        views.add(toBackhaulClaimView(item))
    }
    return views
}

fun sumBackhaulClaimView(view: BackhaulClaimView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedRetries.toDouble() + parsedEtaMinutes.toDouble()
}
