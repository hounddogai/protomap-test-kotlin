package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy service plan baseline. */
data class LegacyServicePlanBaselineView(
    var code: String,
    var name: String,
    var endsAtText: String,
    var vehicleId: String,
    var ownerId: String,
    var amountText: String,
    var retries: String,
    var thresholdText: String,
    var externalRef: String,
    var description: String,
    var quantity: String,
    var statusLabel: String,
)

fun toLegacyServicePlanBaselineView(item: LegacyServicePlanBaseline): LegacyServicePlanBaselineView {
    return LegacyServicePlanBaselineView(
        code = item.code,
        name = item.name,
        endsAtText = item.endsAt.toString(),
        vehicleId = item.vehicleId,
        ownerId = truncate(item.ownerId, 64),
        amountText = "%.2f".format(item.amount),
        retries = item.retries.toString(),
        thresholdText = "%.2f".format(item.threshold),
        externalRef = truncate(item.externalRef, 80),
        description = truncate(item.description, 40),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyServicePlanBaselineViews(items: MutableList<LegacyServicePlanBaseline>): MutableList<LegacyServicePlanBaselineView> {
    val views = mutableListOf<LegacyServicePlanBaselineView>()
    for (item in items) {
        views.add(toLegacyServicePlanBaselineView(item))
    }
    return views
}

fun sumLegacyServicePlanBaselineView(view: LegacyServicePlanBaselineView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedEndsAt.toDouble() + parsedAmount + parsedRetries.toDouble()
}
