package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional qualification claim. */
data class RegionalQualificationClaimView(
    var code: String,
    var nameText: String,
    var quantity: String,
    var distanceKm: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toRegionalQualificationClaimView(item: RegionalQualificationClaim): RegionalQualificationClaimView {
    return RegionalQualificationClaimView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 80),
        quantity = item.quantity.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalQualificationClaimViews(items: MutableList<RegionalQualificationClaim>): MutableList<RegionalQualificationClaimView> {
    val views = mutableListOf<RegionalQualificationClaimView>()
    for (item in items) {
        views.add(toRegionalQualificationClaimView(item))
    }
    return views
}

fun sumRegionalQualificationClaimView(view: RegionalQualificationClaimView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedQuantity.toDouble() + parsedDistanceKm
}
