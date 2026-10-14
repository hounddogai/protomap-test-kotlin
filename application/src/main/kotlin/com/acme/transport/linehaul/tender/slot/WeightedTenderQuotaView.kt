package com.acme.transport.linehaul.tender.slot

/** How screens and exports show a weighted tender quota. */
data class WeightedTenderQuotaView(
    var code: String,
    var titleText: String,
    var floorValue: String,
    var marginText: String,
    var summaryText: String,
    var weight: String,
    var versionText: String,
    var countryText: String,
    var statusLabel: String,
)

fun toWeightedTenderQuotaView(item: WeightedTenderQuota): WeightedTenderQuotaView {
    return WeightedTenderQuotaView(
        code = item.code,
        titleText = item.title,
        floorValue = "%.2f".format(item.floorValue),
        marginText = "%.2f".format(item.margin),
        summaryText = item.summary,
        weight = "%.2f".format(item.weight),
        versionText = item.version.toString(),
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTenderQuotaViews(items: MutableList<WeightedTenderQuota>): MutableList<WeightedTenderQuotaView> {
    val views = mutableListOf<WeightedTenderQuotaView>()
    for (item in items) {
        views.add(toWeightedTenderQuotaView(item))
    }
    return views
}

fun sumWeightedTenderQuotaView(view: WeightedTenderQuotaView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedMargin = view.marginText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedFloorValue + parsedMargin + parsedWeight
}
