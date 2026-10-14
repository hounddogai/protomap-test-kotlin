package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy dispute template. */
data class LegacyDisputeTemplateView(
    var code: String,
    var labelText: String,
    var endsAt: String,
    var cost: String,
    var ratioText: String,
    var ceilingText: String,
    var priorityText: String,
    var limitText: String,
    var vehicleId: String,
    var category: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toLegacyDisputeTemplateView(item: LegacyDisputeTemplate): LegacyDisputeTemplateView {
    return LegacyDisputeTemplateView(
        code = item.code,
        labelText = item.label,
        endsAt = item.endsAt.toString(),
        cost = "%.2f".format(item.cost),
        ratioText = "%.2f".format(item.ratio),
        ceilingText = "%.2f".format(item.ceiling),
        priorityText = item.priority.toString(),
        limitText = item.limit.toString(),
        vehicleId = truncate(item.vehicleId, 40),
        category = item.category,
        driverIdText = truncate(item.driverId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDisputeTemplateViews(items: MutableList<LegacyDisputeTemplate>): MutableList<LegacyDisputeTemplateView> {
    val views = mutableListOf<LegacyDisputeTemplateView>()
    for (item in items) {
        views.add(toLegacyDisputeTemplateView(item))
    }
    return views
}

fun sumLegacyDisputeTemplateView(view: LegacyDisputeTemplateView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedEndsAt.toDouble() + parsedCost + parsedRatio
}
