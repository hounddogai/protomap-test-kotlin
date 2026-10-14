package com.acme.transport.linehaul.relay.baseline

/** How screens and exports show a manual relay baseline. */
data class ManualRelayBaselineView(
    var codeText: String,
    var name: String,
    var windowDays: String,
    var carrier: String,
    var fuelLiters: String,
    var sequenceText: String,
    var laneText: String,
    var statusLabel: String,
)

fun toManualRelayBaselineView(item: ManualRelayBaseline): ManualRelayBaselineView {
    return ManualRelayBaselineView(
        codeText = item.code,
        name = item.name,
        windowDays = item.windowDays.toString(),
        carrier = item.carrier,
        fuelLiters = "%.2f".format(item.fuelLiters),
        sequenceText = item.sequence.toString(),
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRelayBaselineViews(items: MutableList<ManualRelayBaseline>): MutableList<ManualRelayBaselineView> {
    val views = mutableListOf<ManualRelayBaselineView>()
    for (item in items) {
        views.add(toManualRelayBaselineView(item))
    }
    return views
}

fun sumManualRelayBaselineView(view: ManualRelayBaselineView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedWindowDays.toDouble() + parsedFuelLiters + parsedSequence.toDouble()
}
