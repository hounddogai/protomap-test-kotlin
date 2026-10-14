package com.acme.transport.customs.tariff.budget

/** How screens and exports show a tariff template. */
data class TariffTemplateView(
    var code: String,
    var nameText: String,
    var windowDaysText: String,
    var distanceKm: String,
    var category: String,
    var priority: String,
    var rate: String,
    var pinned: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toTariffTemplateView(item: TariffTemplate): TariffTemplateView {
    return TariffTemplateView(
        code = item.code,
        nameText = item.name,
        windowDaysText = item.windowDays.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        category = item.category,
        priority = item.priority.toString(),
        rate = "%.2f".format(item.rate),
        pinned = "${item.pinned}",
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffTemplateViews(items: MutableList<TariffTemplate>): MutableList<TariffTemplateView> {
    val views = mutableListOf<TariffTemplateView>()
    for (item in items) {
        views.add(toTariffTemplateView(item))
    }
    return views
}

fun sumTariffTemplateView(view: TariffTemplateView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedWindowDays.toDouble() + parsedDistanceKm + parsedPriority.toDouble()
}
