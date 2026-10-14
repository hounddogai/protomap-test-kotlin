package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default cutoff agreement. */
data class DefaultCutoffAgreementView(
    var code: String,
    var title: String,
    var durationMinutes: String,
    var createdAt: String,
    var currency: String,
    var depot: String,
    var quantity: String,
    var costText: String,
    var distanceKm: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toDefaultCutoffAgreementView(item: DefaultCutoffAgreement): DefaultCutoffAgreementView {
    return DefaultCutoffAgreementView(
        code = item.code,
        title = truncate(item.title, 120),
        durationMinutes = item.durationMinutes.toString(),
        createdAt = item.createdAt.toString(),
        currency = truncate(item.currency, 80),
        depot = item.depot,
        quantity = item.quantity.toString(),
        costText = "%.2f".format(item.cost),
        distanceKm = "%.2f".format(item.distanceKm),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCutoffAgreementViews(items: MutableList<DefaultCutoffAgreement>): MutableList<DefaultCutoffAgreementView> {
    val views = mutableListOf<DefaultCutoffAgreementView>()
    for (item in items) {
        views.add(toDefaultCutoffAgreementView(item))
    }
    return views
}

fun sumDefaultCutoffAgreementView(view: DefaultCutoffAgreementView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedDurationMinutes.toDouble() + parsedCreatedAt.toDouble() + parsedQuantity.toDouble()
}
