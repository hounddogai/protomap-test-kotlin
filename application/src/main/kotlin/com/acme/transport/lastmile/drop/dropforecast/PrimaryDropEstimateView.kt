package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary drop estimate. */
data class PrimaryDropEstimateView(
    var code: String,
    var title: String,
    var locale: String,
    var palletsText: String,
    var version: String,
    var statusLabel: String,
)

fun toPrimaryDropEstimateView(item: PrimaryDropEstimate): PrimaryDropEstimateView {
    return PrimaryDropEstimateView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 64),
        locale = item.locale,
        palletsText = item.pallets.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDropEstimateViews(items: MutableList<PrimaryDropEstimate>): MutableList<PrimaryDropEstimateView> {
    val views = mutableListOf<PrimaryDropEstimateView>()
    for (item in items) {
        views.add(toPrimaryDropEstimateView(item))
    }
    return views
}

fun sumPrimaryDropEstimateView(view: PrimaryDropEstimateView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedVersion = view.version.toInt()
    return parsedPallets.toDouble() + parsedVersion.toDouble()
}
