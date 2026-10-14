package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock adjustment. */
data class CrossDockAdjustmentView(
    var code: String,
    var name: String,
    var expiresAt: String,
    var etaMinutes: String,
    var country: String,
    var windowDays: String,
    var stopCount: String,
    var driverIdText: String,
    var labelText: String,
    var endsAt: String,
    var cost: String,
    var statusLabel: String,
)

fun toCrossDockAdjustmentView(item: CrossDockAdjustment): CrossDockAdjustmentView {
    return CrossDockAdjustmentView(
        code = item.code,
        name = item.name,
        expiresAt = item.expiresAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        country = item.country,
        windowDays = item.windowDays.toString(),
        stopCount = item.stopCount.toString(),
        driverIdText = truncate(item.driverId, 80),
        labelText = truncate(item.label, 80),
        endsAt = item.endsAt.toString(),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockAdjustmentViews(items: MutableList<CrossDockAdjustment>): MutableList<CrossDockAdjustmentView> {
    val views = mutableListOf<CrossDockAdjustmentView>()
    for (item in items) {
        views.add(toCrossDockAdjustmentView(item))
    }
    return views
}

fun sumCrossDockAdjustmentView(view: CrossDockAdjustmentView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedExpiresAt.toDouble() + parsedEtaMinutes.toDouble() + parsedWindowDays.toDouble()
}
