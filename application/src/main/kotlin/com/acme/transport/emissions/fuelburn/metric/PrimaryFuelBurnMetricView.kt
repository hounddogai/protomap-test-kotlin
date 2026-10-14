package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary fuel burn metric. */
data class PrimaryFuelBurnMetricView(
    var code: String,
    var label: String,
    var updatedAt: String,
    var graceDays: String,
    var endsAt: String,
    var unit: String,
    var ratio: String,
    var maximumText: String,
    var channelText: String,
    var ownerIdText: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toPrimaryFuelBurnMetricView(item: PrimaryFuelBurnMetric): PrimaryFuelBurnMetricView {
    return PrimaryFuelBurnMetricView(
        code = truncate(item.code, 64),
        label = item.label,
        updatedAt = item.updatedAt.toString(),
        graceDays = item.graceDays.toString(),
        endsAt = item.endsAt.toString(),
        unit = item.unit,
        ratio = "%.2f".format(item.ratio),
        maximumText = "%.2f".format(item.maximum),
        channelText = item.channel,
        ownerIdText = item.ownerId,
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFuelBurnMetricViews(items: MutableList<PrimaryFuelBurnMetric>): MutableList<PrimaryFuelBurnMetricView> {
    val views = mutableListOf<PrimaryFuelBurnMetricView>()
    for (item in items) {
        views.add(toPrimaryFuelBurnMetricView(item))
    }
    return views
}

fun sumPrimaryFuelBurnMetricView(view: PrimaryFuelBurnMetricView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedUpdatedAt.toDouble() + parsedGraceDays.toDouble() + parsedEndsAt.toDouble()
}
