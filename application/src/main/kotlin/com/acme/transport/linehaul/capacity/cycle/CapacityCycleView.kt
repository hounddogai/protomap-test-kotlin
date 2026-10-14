package com.acme.transport.linehaul.capacity.cycle

/** How screens and exports show a capacity cycle. */
data class CapacityCycleView(
    var code: String,
    var label: String,
    var trailerId: String,
    var margin: String,
    var archivedText: String,
    var updatedAt: String,
    var expiresAtText: String,
    var cost: String,
    var odometer: String,
    var phone: String,
    var priority: String,
    var statusLabel: String,
)

fun toCapacityCycleView(item: CapacityCycle): CapacityCycleView {
    return CapacityCycleView(
        code = item.code,
        label = item.label,
        trailerId = item.trailerId,
        margin = "%.2f".format(item.margin),
        archivedText = "${item.archived}",
        updatedAt = item.updatedAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        cost = "%.2f".format(item.cost),
        odometer = item.odometer.toString(),
        phone = item.phone,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityCycleViews(items: MutableList<CapacityCycle>): MutableList<CapacityCycleView> {
    val views = mutableListOf<CapacityCycleView>()
    for (item in items) {
        views.add(toCapacityCycleView(item))
    }
    return views
}

fun sumCapacityCycleView(view: CapacityCycleView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedMargin + parsedUpdatedAt.toDouble() + parsedExpiresAt.toDouble()
}
