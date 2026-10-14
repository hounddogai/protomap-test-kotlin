package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional dispute plan. */
data class RegionalDisputePlanView(
    var code: String,
    var label: String,
    var vehicleId: String,
    var limitText: String,
    var attempts: String,
    var margin: String,
    var checksumText: String,
    var country: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toRegionalDisputePlanView(item: RegionalDisputePlan): RegionalDisputePlanView {
    return RegionalDisputePlanView(
        code = item.code,
        label = truncate(item.label, 120),
        vehicleId = item.vehicleId,
        limitText = item.limit.toString(),
        attempts = item.attempts.toString(),
        margin = "%.2f".format(item.margin),
        checksumText = item.checksum,
        country = item.country,
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDisputePlanViews(items: MutableList<RegionalDisputePlan>): MutableList<RegionalDisputePlanView> {
    val views = mutableListOf<RegionalDisputePlanView>()
    for (item in items) {
        views.add(toRegionalDisputePlanView(item))
    }
    return views
}

fun sumRegionalDisputePlanView(view: RegionalDisputePlanView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedLimit.toDouble() + parsedAttempts.toDouble() + parsedMargin
}
