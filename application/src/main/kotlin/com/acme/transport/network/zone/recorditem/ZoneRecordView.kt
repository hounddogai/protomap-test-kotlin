package com.acme.transport.network.zone.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone record. */
data class ZoneRecordView(
    var codeText: String,
    var name: String,
    var timezoneText: String,
    var vehicleId: String,
    var routeCode: String,
    var expiresAt: String,
    var durationMinutes: String,
    var titleText: String,
    var version: String,
    var ratio: String,
    var statusLabel: String,
)

fun toZoneRecordView(item: ZoneRecord): ZoneRecordView {
    return ZoneRecordView(
        codeText = item.code,
        name = truncate(item.name, 80),
        timezoneText = item.timezone,
        vehicleId = item.vehicleId,
        routeCode = item.routeCode,
        expiresAt = item.expiresAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        titleText = item.title,
        version = item.version.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneRecordViews(items: MutableList<ZoneRecord>): MutableList<ZoneRecordView> {
    val views = mutableListOf<ZoneRecordView>()
    for (item in items) {
        views.add(toZoneRecordView(item))
    }
    return views
}

fun sumZoneRecordView(view: ZoneRecordView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedVersion = view.version.toInt()
    return parsedExpiresAt.toDouble() + parsedDurationMinutes.toDouble() + parsedVersion.toDouble()
}
