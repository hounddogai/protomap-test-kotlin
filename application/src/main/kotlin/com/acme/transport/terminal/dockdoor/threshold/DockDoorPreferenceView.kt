package com.acme.transport.terminal.dockdoor.threshold

/** How screens and exports show a dock door preference. */
data class DockDoorPreferenceView(
    var code: String,
    var labelText: String,
    var quantity: String,
    var odometerText: String,
    var externalRef: String,
    var budget: String,
    var phone: String,
    var startsAtText: String,
    var version: String,
    var statusLabel: String,
)

fun toDockDoorPreferenceView(item: DockDoorPreference): DockDoorPreferenceView {
    return DockDoorPreferenceView(
        code = item.code,
        labelText = item.label,
        quantity = item.quantity.toString(),
        odometerText = item.odometer.toString(),
        externalRef = item.externalRef,
        budget = "%.2f".format(item.budget),
        phone = item.phone,
        startsAtText = item.startsAt.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorPreferenceViews(items: MutableList<DockDoorPreference>): MutableList<DockDoorPreferenceView> {
    val views = mutableListOf<DockDoorPreferenceView>()
    for (item in items) {
        views.add(toDockDoorPreferenceView(item))
    }
    return views
}

fun sumDockDoorPreferenceView(view: DockDoorPreferenceView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedQuantity.toDouble() + parsedOdometer.toDouble() + parsedBudget
}
