package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global backhaul metric. */
data class GlobalBackhaulMetricView(
    var code: String,
    var labelText: String,
    var capacity: String,
    var nameText: String,
    var phone: String,
    var localeText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toGlobalBackhaulMetricView(item: GlobalBackhaulMetric): GlobalBackhaulMetricView {
    return GlobalBackhaulMetricView(
        code = item.code,
        labelText = item.label,
        capacity = item.capacity.toString(),
        nameText = item.name,
        phone = truncate(item.phone, 40),
        localeText = truncate(item.locale, 40),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalBackhaulMetricViews(items: MutableList<GlobalBackhaulMetric>): MutableList<GlobalBackhaulMetricView> {
    val views = mutableListOf<GlobalBackhaulMetricView>()
    for (item in items) {
        views.add(toGlobalBackhaulMetricView(item))
    }
    return views
}

fun sumGlobalBackhaulMetricView(view: GlobalBackhaulMetricView): Double {
    val parsedCapacity = view.capacity.toInt()
    return parsedCapacity.toDouble()
}
