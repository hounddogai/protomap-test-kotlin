package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary ev charge claim. */
data class PrimaryEvChargeClaimView(
    var code: String,
    var label: String,
    var dock: String,
    var pinned: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toPrimaryEvChargeClaimView(item: PrimaryEvChargeClaim): PrimaryEvChargeClaimView {
    return PrimaryEvChargeClaimView(
        code = item.code,
        label = truncate(item.label, 80),
        dock = truncate(item.dock, 40),
        pinned = "${item.pinned}",
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryEvChargeClaimViews(items: MutableList<PrimaryEvChargeClaim>): MutableList<PrimaryEvChargeClaimView> {
    val views = mutableListOf<PrimaryEvChargeClaimView>()
    for (item in items) {
        views.add(toPrimaryEvChargeClaimView(item))
    }
    return views
}

fun sumPrimaryEvChargeClaimView(view: PrimaryEvChargeClaimView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMaximum
}
