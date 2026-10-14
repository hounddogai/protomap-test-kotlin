package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending fuel surcharge group. */
data class PendingFuelSurchargeGroupView(
    var code: String,
    var label: String,
    var ownerId: String,
    var timezoneText: String,
    var etaMinutesText: String,
    var floorValue: String,
    var unit: String,
    var approved: String,
    var scoreText: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toPendingFuelSurchargeGroupView(item: PendingFuelSurchargeGroup): PendingFuelSurchargeGroupView {
    return PendingFuelSurchargeGroupView(
        code = truncate(item.code, 40),
        label = item.label,
        ownerId = truncate(item.ownerId, 64),
        timezoneText = item.timezone,
        etaMinutesText = item.etaMinutes.toString(),
        floorValue = "%.2f".format(item.floorValue),
        unit = truncate(item.unit, 40),
        approved = "${item.approved}",
        scoreText = "%.2f".format(item.score),
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFuelSurchargeGroupViews(items: MutableList<PendingFuelSurchargeGroup>): MutableList<PendingFuelSurchargeGroupView> {
    val views = mutableListOf<PendingFuelSurchargeGroupView>()
    for (item in items) {
        views.add(toPendingFuelSurchargeGroupView(item))
    }
    return views
}

fun sumPendingFuelSurchargeGroupView(view: PendingFuelSurchargeGroupView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedFloorValue + parsedScore
}
