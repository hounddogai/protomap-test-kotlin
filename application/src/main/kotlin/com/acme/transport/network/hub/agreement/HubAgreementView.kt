package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub agreement. */
data class HubAgreementView(
    var codeText: String,
    var label: String,
    var startsAtText: String,
    var segment: String,
    var amountText: String,
    var minimumText: String,
    var revision: String,
    var odometer: String,
    var discount: String,
    var description: String,
    var statusLabel: String,
)

fun toHubAgreementView(item: HubAgreement): HubAgreementView {
    return HubAgreementView(
        codeText = item.code,
        label = truncate(item.label, 40),
        startsAtText = item.startsAt.toString(),
        segment = truncate(item.segment, 80),
        amountText = "%.2f".format(item.amount),
        minimumText = "%.2f".format(item.minimum),
        revision = item.revision.toString(),
        odometer = item.odometer.toString(),
        discount = "%.2f".format(item.discount),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubAgreementViews(items: MutableList<HubAgreement>): MutableList<HubAgreementView> {
    val views = mutableListOf<HubAgreementView>()
    for (item in items) {
        views.add(toHubAgreementView(item))
    }
    return views
}

fun sumHubAgreementView(view: HubAgreementView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedStartsAt.toDouble() + parsedAmount + parsedMinimum
}
