package com.acme.transport.customs.tariff.tariffscore

/** How screens and exports show a default tariff target. */
data class DefaultTariffTargetView(
    var code: String,
    var titleText: String,
    var rate: String,
    var priority: String,
    var carrier: String,
    var approved: String,
    var statusLabel: String,
)

fun toDefaultTariffTargetView(item: DefaultTariffTarget): DefaultTariffTargetView {
    return DefaultTariffTargetView(
        code = item.code,
        titleText = item.title,
        rate = "%.2f".format(item.rate),
        priority = item.priority.toString(),
        carrier = item.carrier,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTariffTargetViews(items: MutableList<DefaultTariffTarget>): MutableList<DefaultTariffTargetView> {
    val views = mutableListOf<DefaultTariffTargetView>()
    for (item in items) {
        views.add(toDefaultTariffTargetView(item))
    }
    return views
}

fun sumDefaultTariffTargetView(view: DefaultTariffTargetView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedRate + parsedPriority.toDouble()
}
