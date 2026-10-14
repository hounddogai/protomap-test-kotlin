package com.acme.transport.terminal.appointment.bundle

/** How screens and exports show a lenient appointment rollup. */
data class LenientAppointmentRollupView(
    var code: String,
    var label: String,
    var summary: String,
    var description: String,
    var batchSizeText: String,
    var active: String,
    var scoreText: String,
    var routeCode: String,
    var capacityText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toLenientAppointmentRollupView(item: LenientAppointmentRollup): LenientAppointmentRollupView {
    return LenientAppointmentRollupView(
        code = item.code,
        label = item.label,
        summary = item.summary,
        description = item.description,
        batchSizeText = item.batchSize.toString(),
        active = "${item.active}",
        scoreText = "%.2f".format(item.score),
        routeCode = item.routeCode,
        capacityText = item.capacity.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAppointmentRollupViews(items: MutableList<LenientAppointmentRollup>): MutableList<LenientAppointmentRollupView> {
    val views = mutableListOf<LenientAppointmentRollupView>()
    for (item in items) {
        views.add(toLenientAppointmentRollupView(item))
    }
    return views
}

fun sumLenientAppointmentRollupView(view: LenientAppointmentRollupView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedBatchSize.toDouble() + parsedScore + parsedCapacity.toDouble()
}
