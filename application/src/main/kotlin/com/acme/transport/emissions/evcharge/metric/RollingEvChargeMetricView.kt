package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling ev charge metric. */
data class RollingEvChargeMetricView(
    var code: String,
    var label: String,
    var quantityText: String,
    var checksum: String,
    var loadKg: String,
    var pallets: String,
    var budgetText: String,
    var currency: String,
    var statusLabel: String,
)

fun toRollingEvChargeMetricView(item: RollingEvChargeMetric): RollingEvChargeMetricView {
    return RollingEvChargeMetricView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 40),
        quantityText = item.quantity.toString(),
        checksum = truncate(item.checksum, 120),
        loadKg = "%.2f".format(item.loadKg),
        pallets = item.pallets.toString(),
        budgetText = "%.2f".format(item.budget),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingEvChargeMetricViews(items: MutableList<RollingEvChargeMetric>): MutableList<RollingEvChargeMetricView> {
    val views = mutableListOf<RollingEvChargeMetricView>()
    for (item in items) {
        views.add(toRollingEvChargeMetricView(item))
    }
    return views
}

fun sumRollingEvChargeMetricView(view: RollingEvChargeMetricView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedQuantity.toDouble() + parsedLoadKg + parsedPallets.toDouble()
}
