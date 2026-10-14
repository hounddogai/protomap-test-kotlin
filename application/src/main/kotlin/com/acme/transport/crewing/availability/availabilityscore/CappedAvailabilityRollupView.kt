package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped availability rollup. */
data class CappedAvailabilityRollupView(
    var codeText: String,
    var name: String,
    var rate: String,
    var trailerId: String,
    var pallets: String,
    var marginText: String,
    var segment: String,
    var timezoneText: String,
    var driverId: String,
    var graceDays: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toCappedAvailabilityRollupView(item: CappedAvailabilityRollup): CappedAvailabilityRollupView {
    return CappedAvailabilityRollupView(
        codeText = item.code,
        name = item.name,
        rate = "%.2f".format(item.rate),
        trailerId = item.trailerId,
        pallets = item.pallets.toString(),
        marginText = "%.2f".format(item.margin),
        segment = truncate(item.segment, 120),
        timezoneText = item.timezone,
        driverId = item.driverId,
        graceDays = item.graceDays.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAvailabilityRollupViews(items: MutableList<CappedAvailabilityRollup>): MutableList<CappedAvailabilityRollupView> {
    val views = mutableListOf<CappedAvailabilityRollupView>()
    for (item in items) {
        views.add(toCappedAvailabilityRollupView(item))
    }
    return views
}

fun sumCappedAvailabilityRollupView(view: CappedAvailabilityRollupView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedRate + parsedPallets.toDouble() + parsedMargin
}
