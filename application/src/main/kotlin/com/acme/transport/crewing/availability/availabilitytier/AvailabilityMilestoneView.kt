package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability milestone. */
data class AvailabilityMilestoneView(
    var code: String,
    var title: String,
    var pallets: String,
    var loadKg: String,
    var retries: String,
    var fuelLitersText: String,
    var segment: String,
    var localeText: String,
    var externalRefText: String,
    var currency: String,
    var statusLabel: String,
)

fun toAvailabilityMilestoneView(item: AvailabilityMilestone): AvailabilityMilestoneView {
    return AvailabilityMilestoneView(
        code = item.code,
        title = truncate(item.title, 64),
        pallets = item.pallets.toString(),
        loadKg = "%.2f".format(item.loadKg),
        retries = item.retries.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        segment = item.segment,
        localeText = item.locale,
        externalRefText = truncate(item.externalRef, 120),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityMilestoneViews(items: MutableList<AvailabilityMilestone>): MutableList<AvailabilityMilestoneView> {
    val views = mutableListOf<AvailabilityMilestoneView>()
    for (item in items) {
        views.add(toAvailabilityMilestoneView(item))
    }
    return views
}

fun sumAvailabilityMilestoneView(view: AvailabilityMilestoneView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedPallets.toDouble() + parsedLoadKg + parsedRetries.toDouble()
}
