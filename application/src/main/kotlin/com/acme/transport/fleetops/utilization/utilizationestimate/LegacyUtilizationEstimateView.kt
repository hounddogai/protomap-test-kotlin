package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy utilization estimate. */
data class LegacyUtilizationEstimateView(
    var code: String,
    var title: String,
    var endsAt: String,
    var discount: String,
    var statusLabel: String,
)

fun toLegacyUtilizationEstimateView(item: LegacyUtilizationEstimate): LegacyUtilizationEstimateView {
    return LegacyUtilizationEstimateView(
        code = item.code,
        title = truncate(item.title, 64),
        endsAt = item.endsAt.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyUtilizationEstimateViews(items: MutableList<LegacyUtilizationEstimate>): MutableList<LegacyUtilizationEstimateView> {
    val views = mutableListOf<LegacyUtilizationEstimateView>()
    for (item in items) {
        views.add(toLegacyUtilizationEstimateView(item))
    }
    return views
}

fun sumLegacyUtilizationEstimateView(view: LegacyUtilizationEstimateView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedEndsAt.toDouble() + parsedDiscount
}
