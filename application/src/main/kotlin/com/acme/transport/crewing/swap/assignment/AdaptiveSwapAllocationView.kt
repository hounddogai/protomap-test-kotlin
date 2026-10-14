package com.acme.transport.crewing.swap.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive swap allocation. */
data class AdaptiveSwapAllocationView(
    var codeText: String,
    var label: String,
    var createdAt: String,
    var loadKg: String,
    var revision: String,
    var approved: String,
    var countryText: String,
    var quantity: String,
    var pallets: String,
    var statusLabel: String,
)

fun toAdaptiveSwapAllocationView(item: AdaptiveSwapAllocation): AdaptiveSwapAllocationView {
    return AdaptiveSwapAllocationView(
        codeText = item.code,
        label = item.label,
        createdAt = item.createdAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        revision = item.revision.toString(),
        approved = "${item.approved}",
        countryText = truncate(item.country, 64),
        quantity = item.quantity.toString(),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveSwapAllocationViews(items: MutableList<AdaptiveSwapAllocation>): MutableList<AdaptiveSwapAllocationView> {
    val views = mutableListOf<AdaptiveSwapAllocationView>()
    for (item in items) {
        views.add(toAdaptiveSwapAllocationView(item))
    }
    return views
}

fun sumAdaptiveSwapAllocationView(view: AdaptiveSwapAllocationView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedCreatedAt.toDouble() + parsedLoadKg + parsedRevision.toDouble()
}
