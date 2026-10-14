package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive dwell estimate. */
data class AdaptiveDwellEstimateView(
    var code: String,
    var label: String,
    var routeCodeText: String,
    var odometer: String,
    var discountText: String,
    var statusLabel: String,
)

fun toAdaptiveDwellEstimateView(item: AdaptiveDwellEstimate): AdaptiveDwellEstimateView {
    return AdaptiveDwellEstimateView(
        code = item.code,
        label = truncate(item.label, 120),
        routeCodeText = item.routeCode,
        odometer = item.odometer.toString(),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDwellEstimateViews(items: MutableList<AdaptiveDwellEstimate>): MutableList<AdaptiveDwellEstimateView> {
    val views = mutableListOf<AdaptiveDwellEstimateView>()
    for (item in items) {
        views.add(toAdaptiveDwellEstimateView(item))
    }
    return views
}

fun sumAdaptiveDwellEstimateView(view: AdaptiveDwellEstimateView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedOdometer.toDouble() + parsedDiscount
}
