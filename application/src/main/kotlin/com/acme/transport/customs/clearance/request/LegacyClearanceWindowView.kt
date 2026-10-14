package com.acme.transport.customs.clearance.request

/** How screens and exports show a legacy clearance window. */
data class LegacyClearanceWindowView(
    var code: String,
    var labelText: String,
    var updatedAtText: String,
    var maximum: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toLegacyClearanceWindowView(item: LegacyClearanceWindow): LegacyClearanceWindowView {
    return LegacyClearanceWindowView(
        code = item.code,
        labelText = item.label,
        updatedAtText = item.updatedAt.toString(),
        maximum = "%.2f".format(item.maximum),
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyClearanceWindowViews(items: MutableList<LegacyClearanceWindow>): MutableList<LegacyClearanceWindowView> {
    val views = mutableListOf<LegacyClearanceWindowView>()
    for (item in items) {
        views.add(toLegacyClearanceWindowView(item))
    }
    return views
}

fun sumLegacyClearanceWindowView(view: LegacyClearanceWindowView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedUpdatedAt.toDouble() + parsedMaximum
}
