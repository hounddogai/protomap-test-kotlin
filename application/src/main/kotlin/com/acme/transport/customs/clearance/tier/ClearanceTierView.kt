package com.acme.transport.customs.clearance.tier

/** How screens and exports show a clearance tier. */
data class ClearanceTierView(
    var code: String,
    var labelText: String,
    var contactEmail: String,
    var minimumText: String,
    var sequence: String,
    var fuelLitersText: String,
    var regionText: String,
    var activeText: String,
    var threshold: String,
    var odometerText: String,
    var notesText: String,
    var statusLabel: String,
)

fun toClearanceTierView(item: ClearanceTier): ClearanceTierView {
    return ClearanceTierView(
        code = item.code,
        labelText = item.label,
        contactEmail = item.contactEmail,
        minimumText = "%.2f".format(item.minimum),
        sequence = item.sequence.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        regionText = item.region,
        activeText = "${item.active}",
        threshold = "%.2f".format(item.threshold),
        odometerText = item.odometer.toString(),
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceTierViews(items: MutableList<ClearanceTier>): MutableList<ClearanceTierView> {
    val views = mutableListOf<ClearanceTierView>()
    for (item in items) {
        views.add(toClearanceTierView(item))
    }
    return views
}

fun sumClearanceTierView(view: ClearanceTierView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedMinimum + parsedSequence.toDouble() + parsedFuelLiters
}
