package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered lane plan request. */
data class TieredLanePlanRequestView(
    var code: String,
    var label: String,
    var maximum: String,
    var depot: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toTieredLanePlanRequestView(item: TieredLanePlanRequest): TieredLanePlanRequestView {
    return TieredLanePlanRequestView(
        code = truncate(item.code, 40),
        label = item.label,
        maximum = "%.2f".format(item.maximum),
        depot = truncate(item.depot, 40),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredLanePlanRequestViews(items: MutableList<TieredLanePlanRequest>): MutableList<TieredLanePlanRequestView> {
    val views = mutableListOf<TieredLanePlanRequestView>()
    for (item in items) {
        views.add(toTieredLanePlanRequestView(item))
    }
    return views
}

fun sumTieredLanePlanRequestView(view: TieredLanePlanRequestView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedMaximum + parsedBatchSize.toDouble()
}
