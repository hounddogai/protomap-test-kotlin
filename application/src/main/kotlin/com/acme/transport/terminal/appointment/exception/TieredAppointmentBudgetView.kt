package com.acme.transport.terminal.appointment.exception

/** How screens and exports show a tiered appointment budget. */
data class TieredAppointmentBudgetView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var ratioText: String,
    var pinned: String,
    var summary: String,
    var statusLabel: String,
)

fun toTieredAppointmentBudgetView(item: TieredAppointmentBudget): TieredAppointmentBudgetView {
    return TieredAppointmentBudgetView(
        code = item.code,
        name = item.name,
        etaMinutes = item.etaMinutes.toString(),
        ratioText = "%.2f".format(item.ratio),
        pinned = "${item.pinned}",
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAppointmentBudgetViews(items: MutableList<TieredAppointmentBudget>): MutableList<TieredAppointmentBudgetView> {
    val views = mutableListOf<TieredAppointmentBudgetView>()
    for (item in items) {
        views.add(toTieredAppointmentBudgetView(item))
    }
    return views
}

fun sumTieredAppointmentBudgetView(view: TieredAppointmentBudgetView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRatio
}
