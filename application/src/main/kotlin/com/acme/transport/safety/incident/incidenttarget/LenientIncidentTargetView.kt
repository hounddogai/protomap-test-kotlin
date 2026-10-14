package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient incident target. */
data class LenientIncidentTargetView(
    var codeText: String,
    var title: String,
    var batchSizeText: String,
    var pinned: String,
    var lane: String,
    var driverIdText: String,
    var shiftCodeText: String,
    var countryText: String,
    var minimum: String,
    var createdAt: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toLenientIncidentTargetView(item: LenientIncidentTarget): LenientIncidentTargetView {
    return LenientIncidentTargetView(
        codeText = item.code,
        title = truncate(item.title, 120),
        batchSizeText = item.batchSize.toString(),
        pinned = "${item.pinned}",
        lane = item.lane,
        driverIdText = item.driverId,
        shiftCodeText = truncate(item.shiftCode, 64),
        countryText = item.country,
        minimum = "%.2f".format(item.minimum),
        createdAt = item.createdAt.toString(),
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientIncidentTargetViews(items: MutableList<LenientIncidentTarget>): MutableList<LenientIncidentTargetView> {
    val views = mutableListOf<LenientIncidentTargetView>()
    for (item in items) {
        views.add(toLenientIncidentTargetView(item))
    }
    return views
}

fun sumLenientIncidentTargetView(view: LenientIncidentTargetView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedBatchSize.toDouble() + parsedMinimum + parsedCreatedAt.toDouble()
}
