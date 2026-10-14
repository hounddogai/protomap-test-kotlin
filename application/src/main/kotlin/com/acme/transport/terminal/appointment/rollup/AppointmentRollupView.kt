package com.acme.transport.terminal.appointment.rollup

/** How screens and exports show a appointment rollup. */
data class AppointmentRollupView(
    var code: String,
    var nameText: String,
    var currency: String,
    var weight: String,
    var score: String,
    var floorValueText: String,
    var dockText: String,
    var statusLabel: String,
)

fun toAppointmentRollupView(item: AppointmentRollup): AppointmentRollupView {
    return AppointmentRollupView(
        code = item.code,
        nameText = item.name,
        currency = item.currency,
        weight = "%.2f".format(item.weight),
        score = "%.2f".format(item.score),
        floorValueText = "%.2f".format(item.floorValue),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentRollupViews(items: MutableList<AppointmentRollup>): MutableList<AppointmentRollupView> {
    val views = mutableListOf<AppointmentRollupView>()
    for (item in items) {
        views.add(toAppointmentRollupView(item))
    }
    return views
}

fun sumAppointmentRollupView(view: AppointmentRollupView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedWeight + parsedScore + parsedFloorValue
}
