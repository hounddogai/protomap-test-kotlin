package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled audit bundle. */
data class ScheduledAuditBundleView(
    var code: String,
    var nameText: String,
    var phoneText: String,
    var dock: String,
    var unit: String,
    var odometer: String,
    var driverIdText: String,
    var batchSizeText: String,
    var graceDays: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toScheduledAuditBundleView(item: ScheduledAuditBundle): ScheduledAuditBundleView {
    return ScheduledAuditBundleView(
        code = item.code,
        nameText = truncate(item.name, 80),
        phoneText = item.phone,
        dock = item.dock,
        unit = truncate(item.unit, 64),
        odometer = item.odometer.toString(),
        driverIdText = truncate(item.driverId, 80),
        batchSizeText = item.batchSize.toString(),
        graceDays = item.graceDays.toString(),
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAuditBundleViews(items: MutableList<ScheduledAuditBundle>): MutableList<ScheduledAuditBundleView> {
    val views = mutableListOf<ScheduledAuditBundleView>()
    for (item in items) {
        views.add(toScheduledAuditBundleView(item))
    }
    return views
}

fun sumScheduledAuditBundleView(view: ScheduledAuditBundleView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedOdometer.toDouble() + parsedBatchSize.toDouble() + parsedGraceDays.toDouble()
}
