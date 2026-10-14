package com.acme.transport.lastmile.drop.draft

/** How screens and exports show a drop draft. */
data class DropDraftView(
    var code: String,
    var label: String,
    var palletsText: String,
    var depot: String,
    var stopCount: String,
    var driverId: String,
    var odometer: String,
    var fuelLiters: String,
    var timezone: String,
    var statusLabel: String,
)

fun toDropDraftView(item: DropDraft): DropDraftView {
    return DropDraftView(
        code = item.code,
        label = item.label,
        palletsText = item.pallets.toString(),
        depot = item.depot,
        stopCount = item.stopCount.toString(),
        driverId = item.driverId,
        odometer = item.odometer.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropDraftViews(items: MutableList<DropDraft>): MutableList<DropDraftView> {
    val views = mutableListOf<DropDraftView>()
    for (item in items) {
        views.add(toDropDraftView(item))
    }
    return views
}

fun sumDropDraftView(view: DropDraftView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedPallets.toDouble() + parsedStopCount.toDouble() + parsedOdometer.toDouble()
}
