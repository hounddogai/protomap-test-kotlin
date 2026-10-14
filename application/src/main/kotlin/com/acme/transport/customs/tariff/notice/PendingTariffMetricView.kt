package com.acme.transport.customs.tariff.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending tariff metric. */
data class PendingTariffMetricView(
    var code: String,
    var name: String,
    var endsAt: String,
    var capacity: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toPendingTariffMetricView(item: PendingTariffMetric): PendingTariffMetricView {
    return PendingTariffMetricView(
        code = item.code,
        name = truncate(item.name, 120),
        endsAt = item.endsAt.toString(),
        capacity = item.capacity.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTariffMetricViews(items: MutableList<PendingTariffMetric>): MutableList<PendingTariffMetricView> {
    val views = mutableListOf<PendingTariffMetricView>()
    for (item in items) {
        views.add(toPendingTariffMetricView(item))
    }
    return views
}

fun sumPendingTariffMetricView(view: PendingTariffMetricView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedEndsAt.toDouble() + parsedCapacity.toDouble() + parsedCeiling
}
