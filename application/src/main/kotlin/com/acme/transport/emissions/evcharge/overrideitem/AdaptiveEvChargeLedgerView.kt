package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive ev charge ledger. */
data class AdaptiveEvChargeLedgerView(
    var code: String,
    var titleText: String,
    var channel: String,
    var vehicleIdText: String,
    var segmentText: String,
    var graceDaysText: String,
    var capacity: String,
    var currency: String,
    var localeText: String,
    var loadKg: String,
    var lane: String,
    var statusLabel: String,
)

fun toAdaptiveEvChargeLedgerView(item: AdaptiveEvChargeLedger): AdaptiveEvChargeLedgerView {
    return AdaptiveEvChargeLedgerView(
        code = item.code,
        titleText = item.title,
        channel = item.channel,
        vehicleIdText = item.vehicleId,
        segmentText = item.segment,
        graceDaysText = item.graceDays.toString(),
        capacity = item.capacity.toString(),
        currency = truncate(item.currency, 64),
        localeText = item.locale,
        loadKg = "%.2f".format(item.loadKg),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveEvChargeLedgerViews(items: MutableList<AdaptiveEvChargeLedger>): MutableList<AdaptiveEvChargeLedgerView> {
    val views = mutableListOf<AdaptiveEvChargeLedgerView>()
    for (item in items) {
        views.add(toAdaptiveEvChargeLedgerView(item))
    }
    return views
}

fun sumAdaptiveEvChargeLedgerView(view: AdaptiveEvChargeLedgerView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedGraceDays.toDouble() + parsedCapacity.toDouble() + parsedLoadKg
}
