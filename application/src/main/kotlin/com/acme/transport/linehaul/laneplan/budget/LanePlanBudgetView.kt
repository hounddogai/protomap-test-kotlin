package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan budget. */
data class LanePlanBudgetView(
    var code: String,
    var name: String,
    var title: String,
    var vehicleId: String,
    var quantity: String,
    var endsAt: String,
    var approved: String,
    var updatedAtText: String,
    var currency: String,
    var externalRef: String,
    var maximum: String,
    var statusLabel: String,
)

fun toLanePlanBudgetView(item: LanePlanBudget): LanePlanBudgetView {
    return LanePlanBudgetView(
        code = item.code,
        name = item.name,
        title = item.title,
        vehicleId = truncate(item.vehicleId, 80),
        quantity = item.quantity.toString(),
        endsAt = item.endsAt.toString(),
        approved = "${item.approved}",
        updatedAtText = item.updatedAt.toString(),
        currency = truncate(item.currency, 120),
        externalRef = item.externalRef,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanBudgetViews(items: MutableList<LanePlanBudget>): MutableList<LanePlanBudgetView> {
    val views = mutableListOf<LanePlanBudgetView>()
    for (item in items) {
        views.add(toLanePlanBudgetView(item))
    }
    return views
}

fun sumLanePlanBudgetView(view: LanePlanBudgetView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedQuantity.toDouble() + parsedEndsAt.toDouble() + parsedUpdatedAt.toDouble()
}
