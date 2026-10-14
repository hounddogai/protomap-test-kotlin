package com.acme.transport.network.transittime.summary

/** How screens and exports show a default transit time quota. */
data class DefaultTransitTimeQuotaView(
    var codeText: String,
    var nameText: String,
    var categoryText: String,
    var floorValue: String,
    var minimum: String,
    var marginText: String,
    var budgetText: String,
    var rate: String,
    var country: String,
    var statusLabel: String,
)

fun toDefaultTransitTimeQuotaView(item: DefaultTransitTimeQuota): DefaultTransitTimeQuotaView {
    return DefaultTransitTimeQuotaView(
        codeText = item.code,
        nameText = item.name,
        categoryText = item.category,
        floorValue = "%.2f".format(item.floorValue),
        minimum = "%.2f".format(item.minimum),
        marginText = "%.2f".format(item.margin),
        budgetText = "%.2f".format(item.budget),
        rate = "%.2f".format(item.rate),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTransitTimeQuotaViews(items: MutableList<DefaultTransitTimeQuota>): MutableList<DefaultTransitTimeQuotaView> {
    val views = mutableListOf<DefaultTransitTimeQuotaView>()
    for (item in items) {
        views.add(toDefaultTransitTimeQuotaView(item))
    }
    return views
}

fun sumDefaultTransitTimeQuotaView(view: DefaultTransitTimeQuotaView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedFloorValue + parsedMinimum + parsedMargin
}
