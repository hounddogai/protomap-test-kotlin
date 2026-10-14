package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched roster cycle. */
data class BatchedRosterCycleView(
    var codeText: String,
    var label: String,
    var graceDays: String,
    var vehicleId: String,
    var etaMinutes: String,
    var odometerText: String,
    var contactEmail: String,
    var titleText: String,
    var statusLabel: String,
)

fun toBatchedRosterCycleView(item: BatchedRosterCycle): BatchedRosterCycleView {
    return BatchedRosterCycleView(
        codeText = item.code,
        label = truncate(item.label, 64),
        graceDays = item.graceDays.toString(),
        vehicleId = item.vehicleId,
        etaMinutes = item.etaMinutes.toString(),
        odometerText = item.odometer.toString(),
        contactEmail = item.contactEmail,
        titleText = truncate(item.title, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRosterCycleViews(items: MutableList<BatchedRosterCycle>): MutableList<BatchedRosterCycleView> {
    val views = mutableListOf<BatchedRosterCycleView>()
    for (item in items) {
        views.add(toBatchedRosterCycleView(item))
    }
    return views
}

fun sumBatchedRosterCycleView(view: BatchedRosterCycleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedGraceDays.toDouble() + parsedEtaMinutes.toDouble() + parsedOdometer.toDouble()
}
