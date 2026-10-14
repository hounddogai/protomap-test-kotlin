package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy utilization variance. */
data class LegacyUtilizationVarianceView(
    var code: String,
    var title: String,
    var budget: String,
    var minimum: String,
    var depotText: String,
    var reviewerText: String,
    var endsAt: String,
    var attempts: String,
    var phone: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toLegacyUtilizationVarianceView(item: LegacyUtilizationVariance): LegacyUtilizationVarianceView {
    return LegacyUtilizationVarianceView(
        code = item.code,
        title = truncate(item.title, 64),
        budget = "%.2f".format(item.budget),
        minimum = "%.2f".format(item.minimum),
        depotText = truncate(item.depot, 40),
        reviewerText = truncate(item.reviewer, 120),
        endsAt = item.endsAt.toString(),
        attempts = item.attempts.toString(),
        phone = truncate(item.phone, 80),
        currencyText = truncate(item.currency, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyUtilizationVarianceViews(items: MutableList<LegacyUtilizationVariance>): MutableList<LegacyUtilizationVarianceView> {
    val views = mutableListOf<LegacyUtilizationVarianceView>()
    for (item in items) {
        views.add(toLegacyUtilizationVarianceView(item))
    }
    return views
}

fun sumLegacyUtilizationVarianceView(view: LegacyUtilizationVarianceView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedBudget + parsedMinimum + parsedEndsAt.toDouble()
}
