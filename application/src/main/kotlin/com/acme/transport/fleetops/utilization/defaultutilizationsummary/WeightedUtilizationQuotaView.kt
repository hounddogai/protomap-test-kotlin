package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** How screens and exports show a weighted utilization quota. */
data class WeightedUtilizationQuotaView(
    var codeText: String,
    var title: String,
    var etaMinutes: String,
    var quantity: String,
    var summaryText: String,
    var ownerId: String,
    var cost: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toWeightedUtilizationQuotaView(item: WeightedUtilizationQuota): WeightedUtilizationQuotaView {
    return WeightedUtilizationQuotaView(
        codeText = item.code,
        title = item.title,
        etaMinutes = item.etaMinutes.toString(),
        quantity = item.quantity.toString(),
        summaryText = item.summary,
        ownerId = item.ownerId,
        cost = "%.2f".format(item.cost),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedUtilizationQuotaViews(items: MutableList<WeightedUtilizationQuota>): MutableList<WeightedUtilizationQuotaView> {
    val views = mutableListOf<WeightedUtilizationQuotaView>()
    for (item in items) {
        views.add(toWeightedUtilizationQuotaView(item))
    }
    return views
}

fun sumWeightedUtilizationQuotaView(view: WeightedUtilizationQuotaView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedEtaMinutes.toDouble() + parsedQuantity.toDouble() + parsedCost
}
