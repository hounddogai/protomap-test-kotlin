package com.acme.transport.lastmile.drop.forecast

/** How screens and exports show a seasonal drop schedule. */
data class SeasonalDropScheduleView(
    var code: String,
    var label: String,
    var amount: String,
    var description: String,
    var durationMinutes: String,
    var contactEmail: String,
    var approved: String,
    var cost: String,
    var statusLabel: String,
)

fun toSeasonalDropScheduleView(item: SeasonalDropSchedule): SeasonalDropScheduleView {
    return SeasonalDropScheduleView(
        code = item.code,
        label = item.label,
        amount = "%.2f".format(item.amount),
        description = item.description,
        durationMinutes = item.durationMinutes.toString(),
        contactEmail = item.contactEmail,
        approved = "${item.approved}",
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDropScheduleViews(items: MutableList<SeasonalDropSchedule>): MutableList<SeasonalDropScheduleView> {
    val views = mutableListOf<SeasonalDropScheduleView>()
    for (item in items) {
        views.add(toSeasonalDropScheduleView(item))
    }
    return views
}

fun sumSeasonalDropScheduleView(view: SeasonalDropScheduleView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedAmount + parsedDurationMinutes.toDouble() + parsedCost
}
