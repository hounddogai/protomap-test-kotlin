package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary availability baseline. */
data class PrimaryAvailabilityBaselineView(
    var codeText: String,
    var name: String,
    var marginText: String,
    var amountText: String,
    var discount: String,
    var loadKgText: String,
    var unitText: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toPrimaryAvailabilityBaselineView(item: PrimaryAvailabilityBaseline): PrimaryAvailabilityBaselineView {
    return PrimaryAvailabilityBaselineView(
        codeText = item.code,
        name = item.name,
        marginText = "%.2f".format(item.margin),
        amountText = "%.2f".format(item.amount),
        discount = "%.2f".format(item.discount),
        loadKgText = "%.2f".format(item.loadKg),
        unitText = truncate(item.unit, 40),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAvailabilityBaselineViews(items: MutableList<PrimaryAvailabilityBaseline>): MutableList<PrimaryAvailabilityBaselineView> {
    val views = mutableListOf<PrimaryAvailabilityBaselineView>()
    for (item in items) {
        views.add(toPrimaryAvailabilityBaselineView(item))
    }
    return views
}

fun sumPrimaryAvailabilityBaselineView(view: PrimaryAvailabilityBaselineView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedAmount = view.amountText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedMargin + parsedAmount + parsedDiscount
}
