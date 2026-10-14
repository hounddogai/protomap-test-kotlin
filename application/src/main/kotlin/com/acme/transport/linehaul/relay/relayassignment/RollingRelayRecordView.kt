package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling relay record. */
data class RollingRelayRecordView(
    var codeText: String,
    var title: String,
    var regionText: String,
    var quantity: String,
    var ceiling: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toRollingRelayRecordView(item: RollingRelayRecord): RollingRelayRecordView {
    return RollingRelayRecordView(
        codeText = truncate(item.code, 120),
        title = item.title,
        regionText = item.region,
        quantity = item.quantity.toString(),
        ceiling = "%.2f".format(item.ceiling),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRelayRecordViews(items: MutableList<RollingRelayRecord>): MutableList<RollingRelayRecordView> {
    val views = mutableListOf<RollingRelayRecordView>()
    for (item in items) {
        views.add(toRollingRelayRecordView(item))
    }
    return views
}

fun sumRollingRelayRecordView(view: RollingRelayRecordView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedQuantity.toDouble() + parsedCeiling
}
