package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold override. */
data class HoldOverrideView(
    var codeText: String,
    var titleText: String,
    var depot: String,
    var contactEmail: String,
    var source: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toHoldOverrideView(item: HoldOverride): HoldOverrideView {
    return HoldOverrideView(
        codeText = truncate(item.code, 40),
        titleText = truncate(item.title, 40),
        depot = truncate(item.depot, 120),
        contactEmail = item.contactEmail,
        source = truncate(item.source, 40),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldOverrideViews(items: MutableList<HoldOverride>): MutableList<HoldOverrideView> {
    val views = mutableListOf<HoldOverrideView>()
    for (item in items) {
        views.add(toHoldOverrideView(item))
    }
    return views
}

fun sumHoldOverrideView(view: HoldOverrideView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
