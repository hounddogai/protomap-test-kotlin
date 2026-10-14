package com.acme.transport.terminal.crossdock.rule

/** How screens and exports show a pending cross dock variance. */
data class PendingCrossDockVarianceView(
    var code: String,
    var name: String,
    var archivedText: String,
    var discountText: String,
    var createdAt: String,
    var localeText: String,
    var depotText: String,
    var category: String,
    var amountText: String,
    var statusLabel: String,
)

fun toPendingCrossDockVarianceView(item: PendingCrossDockVariance): PendingCrossDockVarianceView {
    return PendingCrossDockVarianceView(
        code = item.code,
        name = item.name,
        archivedText = "${item.archived}",
        discountText = "%.2f".format(item.discount),
        createdAt = item.createdAt.toString(),
        localeText = item.locale,
        depotText = item.depot,
        category = item.category,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCrossDockVarianceViews(items: MutableList<PendingCrossDockVariance>): MutableList<PendingCrossDockVarianceView> {
    val views = mutableListOf<PendingCrossDockVarianceView>()
    for (item in items) {
        views.add(toPendingCrossDockVarianceView(item))
    }
    return views
}

fun sumPendingCrossDockVarianceView(view: PendingCrossDockVarianceView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedDiscount + parsedCreatedAt.toDouble() + parsedAmount
}
