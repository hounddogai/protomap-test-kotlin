package com.acme.transport.terminal.dwell.preference

/** How screens and exports show a dwell plan. */
data class DwellPlanView(
    var codeText: String,
    var titleText: String,
    var quantityText: String,
    var rate: String,
    var updatedAt: String,
    var pinned: String,
    var cost: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toDwellPlanView(item: DwellPlan): DwellPlanView {
    return DwellPlanView(
        codeText = item.code,
        titleText = item.title,
        quantityText = item.quantity.toString(),
        rate = "%.2f".format(item.rate),
        updatedAt = item.updatedAt.toString(),
        pinned = "${item.pinned}",
        cost = "%.2f".format(item.cost),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellPlanViews(items: MutableList<DwellPlan>): MutableList<DwellPlanView> {
    val views = mutableListOf<DwellPlanView>()
    for (item in items) {
        views.add(toDwellPlanView(item))
    }
    return views
}

fun sumDwellPlanView(view: DwellPlanView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedQuantity.toDouble() + parsedRate + parsedUpdatedAt.toDouble()
}
