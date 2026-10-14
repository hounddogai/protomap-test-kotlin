package com.acme.transport.emissions.evcharge.agreement

/** How screens and exports show a ev charge segment. */
data class EvChargeSegmentView(
    var codeText: String,
    var name: String,
    var odometer: String,
    var visible: String,
    var updatedAtText: String,
    var etaMinutes: String,
    var minimum: String,
    var notes: String,
    var quantity: String,
    var statusLabel: String,
)

fun toEvChargeSegmentView(item: EvChargeSegment): EvChargeSegmentView {
    return EvChargeSegmentView(
        codeText = item.code,
        name = item.name,
        odometer = item.odometer.toString(),
        visible = "${item.visible}",
        updatedAtText = item.updatedAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        notes = item.notes,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeSegmentViews(items: MutableList<EvChargeSegment>): MutableList<EvChargeSegmentView> {
    val views = mutableListOf<EvChargeSegmentView>()
    for (item in items) {
        views.add(toEvChargeSegmentView(item))
    }
    return views
}

fun sumEvChargeSegmentView(view: EvChargeSegmentView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedOdometer.toDouble() + parsedUpdatedAt.toDouble() + parsedEtaMinutes.toDouble()
}
