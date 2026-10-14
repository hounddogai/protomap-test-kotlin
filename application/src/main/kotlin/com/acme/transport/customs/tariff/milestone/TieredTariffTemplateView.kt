package com.acme.transport.customs.tariff.milestone

/** How screens and exports show a tiered tariff template. */
data class TieredTariffTemplateView(
    var code: String,
    var title: String,
    var sequence: String,
    var version: String,
    var discount: String,
    var budgetText: String,
    var maximum: String,
    var updatedAtText: String,
    var retries: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toTieredTariffTemplateView(item: TieredTariffTemplate): TieredTariffTemplateView {
    return TieredTariffTemplateView(
        code = item.code,
        title = item.title,
        sequence = item.sequence.toString(),
        version = item.version.toString(),
        discount = "%.2f".format(item.discount),
        budgetText = "%.2f".format(item.budget),
        maximum = "%.2f".format(item.maximum),
        updatedAtText = item.updatedAt.toString(),
        retries = item.retries.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTariffTemplateViews(items: MutableList<TieredTariffTemplate>): MutableList<TieredTariffTemplateView> {
    val views = mutableListOf<TieredTariffTemplateView>()
    for (item in items) {
        views.add(toTieredTariffTemplateView(item))
    }
    return views
}

fun sumTieredTariffTemplateView(view: TieredTariffTemplateView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedVersion = view.version.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedSequence.toDouble() + parsedVersion.toDouble() + parsedDiscount
}
