package com.acme.transport.fleetops.tire.profile

/** How screens and exports show a tire score. */
data class TireScoreView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var visibleText: String,
    var ownerId: String,
    var phone: String,
    var budget: String,
    var statusLabel: String,
)

fun toTireScoreView(item: TireScore): TireScoreView {
    return TireScoreView(
        code = item.code,
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        visibleText = "${item.visible}",
        ownerId = item.ownerId,
        phone = item.phone,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireScoreViews(items: MutableList<TireScore>): MutableList<TireScoreView> {
    val views = mutableListOf<TireScoreView>()
    for (item in items) {
        views.add(toTireScoreView(item))
    }
    return views
}

fun sumTireScoreView(view: TireScoreView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedEtaMinutes.toDouble() + parsedBudget
}
