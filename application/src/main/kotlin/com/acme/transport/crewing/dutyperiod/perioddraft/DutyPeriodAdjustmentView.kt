package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period adjustment. */
data class DutyPeriodAdjustmentView(
    var code: String,
    var label: String,
    var channel: String,
    var rate: String,
    var discount: String,
    var pallets: String,
    var vehicleIdText: String,
    var source: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toDutyPeriodAdjustmentView(item: DutyPeriodAdjustment): DutyPeriodAdjustmentView {
    return DutyPeriodAdjustmentView(
        code = item.code,
        label = truncate(item.label, 40),
        channel = truncate(item.channel, 120),
        rate = "%.2f".format(item.rate),
        discount = "%.2f".format(item.discount),
        pallets = item.pallets.toString(),
        vehicleIdText = item.vehicleId,
        source = item.source,
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodAdjustmentViews(items: MutableList<DutyPeriodAdjustment>): MutableList<DutyPeriodAdjustmentView> {
    val views = mutableListOf<DutyPeriodAdjustmentView>()
    for (item in items) {
        views.add(toDutyPeriodAdjustmentView(item))
    }
    return views
}

fun sumDutyPeriodAdjustmentView(view: DutyPeriodAdjustmentView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedRate + parsedDiscount + parsedPallets.toDouble()
}
