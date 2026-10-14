package com.acme.transport.linehaul.tender.group

/** How screens and exports show a strict tender profile. */
data class StrictTenderProfileView(
    var code: String,
    var labelText: String,
    var startsAt: String,
    var capacity: String,
    var budget: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toStrictTenderProfileView(item: StrictTenderProfile): StrictTenderProfileView {
    return StrictTenderProfileView(
        code = item.code,
        labelText = item.label,
        startsAt = item.startsAt.toString(),
        capacity = item.capacity.toString(),
        budget = "%.2f".format(item.budget),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTenderProfileViews(items: MutableList<StrictTenderProfile>): MutableList<StrictTenderProfileView> {
    val views = mutableListOf<StrictTenderProfileView>()
    for (item in items) {
        views.add(toStrictTenderProfileView(item))
    }
    return views
}

fun sumStrictTenderProfileView(view: StrictTenderProfileView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedStartsAt.toDouble() + parsedCapacity.toDouble() + parsedBudget
}
