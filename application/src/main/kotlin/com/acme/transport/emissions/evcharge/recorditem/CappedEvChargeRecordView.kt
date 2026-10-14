package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped ev charge record. */
data class CappedEvChargeRecordView(
    var code: String,
    var label: String,
    var vehicleId: String,
    var routeCode: String,
    var endsAtText: String,
    var laneText: String,
    var timezone: String,
    var source: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toCappedEvChargeRecordView(item: CappedEvChargeRecord): CappedEvChargeRecordView {
    return CappedEvChargeRecordView(
        code = truncate(item.code, 64),
        label = item.label,
        vehicleId = item.vehicleId,
        routeCode = truncate(item.routeCode, 120),
        endsAtText = item.endsAt.toString(),
        laneText = item.lane,
        timezone = truncate(item.timezone, 120),
        source = truncate(item.source, 80),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedEvChargeRecordViews(items: MutableList<CappedEvChargeRecord>): MutableList<CappedEvChargeRecordView> {
    val views = mutableListOf<CappedEvChargeRecordView>()
    for (item in items) {
        views.add(toCappedEvChargeRecordView(item))
    }
    return views
}

fun sumCappedEvChargeRecordView(view: CappedEvChargeRecordView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedEndsAt.toDouble() + parsedExpiresAt.toDouble()
}
