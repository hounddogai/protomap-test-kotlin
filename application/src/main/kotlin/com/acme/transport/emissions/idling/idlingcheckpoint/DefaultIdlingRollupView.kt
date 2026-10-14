package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default idling rollup. */
data class DefaultIdlingRollupView(
    var codeText: String,
    var titleText: String,
    var etaMinutes: String,
    var retries: String,
    var checksum: String,
    var minimumText: String,
    var contactEmailText: String,
    var vehicleIdText: String,
    var channel: String,
    var windowDays: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toDefaultIdlingRollupView(item: DefaultIdlingRollup): DefaultIdlingRollupView {
    return DefaultIdlingRollupView(
        codeText = item.code,
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        retries = item.retries.toString(),
        checksum = truncate(item.checksum, 40),
        minimumText = "%.2f".format(item.minimum),
        contactEmailText = item.contactEmail,
        vehicleIdText = item.vehicleId,
        channel = truncate(item.channel, 80),
        windowDays = item.windowDays.toString(),
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultIdlingRollupViews(items: MutableList<DefaultIdlingRollup>): MutableList<DefaultIdlingRollupView> {
    val views = mutableListOf<DefaultIdlingRollupView>()
    for (item in items) {
        views.add(toDefaultIdlingRollupView(item))
    }
    return views
}

fun sumDefaultIdlingRollupView(view: DefaultIdlingRollupView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRetries.toDouble() + parsedMinimum
}
