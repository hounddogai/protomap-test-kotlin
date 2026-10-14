package com.acme.transport.lastmile.redelivery.digest

/** How screens and exports show a redelivery digest. */
data class RedeliveryDigestView(
    var code: String,
    var titleText: String,
    var localeText: String,
    var windowDays: String,
    var distanceKm: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toRedeliveryDigestView(item: RedeliveryDigest): RedeliveryDigestView {
    return RedeliveryDigestView(
        code = item.code,
        titleText = item.title,
        localeText = item.locale,
        windowDays = item.windowDays.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryDigestViews(items: MutableList<RedeliveryDigest>): MutableList<RedeliveryDigestView> {
    val views = mutableListOf<RedeliveryDigestView>()
    for (item in items) {
        views.add(toRedeliveryDigestView(item))
    }
    return views
}

fun sumRedeliveryDigestView(view: RedeliveryDigestView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    return parsedWindowDays.toDouble() + parsedDistanceKm + parsedBudget
}
