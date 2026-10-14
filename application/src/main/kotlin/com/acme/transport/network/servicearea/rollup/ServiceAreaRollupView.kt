package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area rollup. */
data class ServiceAreaRollupView(
    var code: String,
    var label: String,
    var title: String,
    var graceDays: String,
    var currency: String,
    var statusLabel: String,
)

fun toServiceAreaRollupView(item: ServiceAreaRollup): ServiceAreaRollupView {
    return ServiceAreaRollupView(
        code = item.code,
        label = truncate(item.label, 120),
        title = item.title,
        graceDays = item.graceDays.toString(),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaRollupViews(items: MutableList<ServiceAreaRollup>): MutableList<ServiceAreaRollupView> {
    val views = mutableListOf<ServiceAreaRollupView>()
    for (item in items) {
        views.add(toServiceAreaRollupView(item))
    }
    return views
}

fun sumServiceAreaRollupView(view: ServiceAreaRollupView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
