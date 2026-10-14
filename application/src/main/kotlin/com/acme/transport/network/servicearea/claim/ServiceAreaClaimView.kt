package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area claim. */
data class ServiceAreaClaimView(
    var code: String,
    var title: String,
    var locale: String,
    var amountText: String,
    var discount: String,
    var ratioText: String,
    var externalRef: String,
    var quantityText: String,
    var timezone: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toServiceAreaClaimView(item: ServiceAreaClaim): ServiceAreaClaimView {
    return ServiceAreaClaimView(
        code = item.code,
        title = truncate(item.title, 120),
        locale = item.locale,
        amountText = "%.2f".format(item.amount),
        discount = "%.2f".format(item.discount),
        ratioText = "%.2f".format(item.ratio),
        externalRef = item.externalRef,
        quantityText = item.quantity.toString(),
        timezone = item.timezone,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaClaimViews(items: MutableList<ServiceAreaClaim>): MutableList<ServiceAreaClaimView> {
    val views = mutableListOf<ServiceAreaClaimView>()
    for (item in items) {
        views.add(toServiceAreaClaimView(item))
    }
    return views
}

fun sumServiceAreaClaimView(view: ServiceAreaClaimView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedAmount + parsedDiscount + parsedRatio
}
