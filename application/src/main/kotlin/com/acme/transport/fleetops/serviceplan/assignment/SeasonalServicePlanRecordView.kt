package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal service plan record. */
data class SeasonalServicePlanRecordView(
    var codeText: String,
    var nameText: String,
    var checksum: String,
    var fuelLiters: String,
    var carrier: String,
    var graceDays: String,
    var lane: String,
    var maximum: String,
    var stopCountText: String,
    var dock: String,
    var region: String,
    var statusLabel: String,
)

fun toSeasonalServicePlanRecordView(item: SeasonalServicePlanRecord): SeasonalServicePlanRecordView {
    return SeasonalServicePlanRecordView(
        codeText = item.code,
        nameText = item.name,
        checksum = item.checksum,
        fuelLiters = "%.2f".format(item.fuelLiters),
        carrier = truncate(item.carrier, 40),
        graceDays = item.graceDays.toString(),
        lane = item.lane,
        maximum = "%.2f".format(item.maximum),
        stopCountText = item.stopCount.toString(),
        dock = item.dock,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalServicePlanRecordViews(items: MutableList<SeasonalServicePlanRecord>): MutableList<SeasonalServicePlanRecordView> {
    val views = mutableListOf<SeasonalServicePlanRecordView>()
    for (item in items) {
        views.add(toSeasonalServicePlanRecordView(item))
    }
    return views
}

fun sumSeasonalServicePlanRecordView(view: SeasonalServicePlanRecordView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedFuelLiters + parsedGraceDays.toDouble() + parsedMaximum
}
