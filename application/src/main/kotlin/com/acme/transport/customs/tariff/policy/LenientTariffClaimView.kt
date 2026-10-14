package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient tariff claim. */
data class LenientTariffClaimView(
    var codeText: String,
    var title: String,
    var lane: String,
    var phone: String,
    var countryText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toLenientTariffClaimView(item: LenientTariffClaim): LenientTariffClaimView {
    return LenientTariffClaimView(
        codeText = truncate(item.code, 120),
        title = truncate(item.title, 120),
        lane = item.lane,
        phone = truncate(item.phone, 64),
        countryText = item.country,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTariffClaimViews(items: MutableList<LenientTariffClaim>): MutableList<LenientTariffClaimView> {
    val views = mutableListOf<LenientTariffClaimView>()
    for (item in items) {
        views.add(toLenientTariffClaimView(item))
    }
    return views
}

fun sumLenientTariffClaimView(view: LenientTariffClaimView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
