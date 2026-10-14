package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area preference. */
data class ServiceAreaPreferenceView(
    var codeText: String,
    var labelText: String,
    var pinnedText: String,
    var sequence: String,
    var summaryText: String,
    var cost: String,
    var timezone: String,
    var quantity: String,
    var endsAt: String,
    var category: String,
    var statusLabel: String,
)

fun toServiceAreaPreferenceView(item: ServiceAreaPreference): ServiceAreaPreferenceView {
    return ServiceAreaPreferenceView(
        codeText = item.code,
        labelText = item.label,
        pinnedText = "${item.pinned}",
        sequence = item.sequence.toString(),
        summaryText = item.summary,
        cost = "%.2f".format(item.cost),
        timezone = truncate(item.timezone, 120),
        quantity = item.quantity.toString(),
        endsAt = item.endsAt.toString(),
        category = truncate(item.category, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaPreferenceViews(items: MutableList<ServiceAreaPreference>): MutableList<ServiceAreaPreferenceView> {
    val views = mutableListOf<ServiceAreaPreferenceView>()
    for (item in items) {
        views.add(toServiceAreaPreferenceView(item))
    }
    return views
}

fun sumServiceAreaPreferenceView(view: ServiceAreaPreferenceView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedSequence.toDouble() + parsedCost + parsedQuantity.toDouble()
}
