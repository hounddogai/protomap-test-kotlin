package com.acme.transport.linehaul.capacity.reservation

/** How screens and exports show a tiered capacity group. */
data class TieredCapacityGroupView(
    var code: String,
    var nameText: String,
    var timezone: String,
    var notes: String,
    var quantityText: String,
    var budget: String,
    var statusLabel: String,
)

fun toTieredCapacityGroupView(item: TieredCapacityGroup): TieredCapacityGroupView {
    return TieredCapacityGroupView(
        code = item.code,
        nameText = item.name,
        timezone = item.timezone,
        notes = item.notes,
        quantityText = item.quantity.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCapacityGroupViews(items: MutableList<TieredCapacityGroup>): MutableList<TieredCapacityGroupView> {
    val views = mutableListOf<TieredCapacityGroupView>()
    for (item in items) {
        views.add(toTieredCapacityGroupView(item))
    }
    return views
}

fun sumTieredCapacityGroupView(view: TieredCapacityGroupView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedQuantity.toDouble() + parsedBudget
}
