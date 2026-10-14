package com.acme.transport.lastmile.redelivery.checkpoint

/** How screens and exports show a redelivery mapping. */
data class RedeliveryMappingView(
    var code: String,
    var name: String,
    var checksumText: String,
    var notes: String,
    var maximum: String,
    var unit: String,
    var statusLabel: String,
)

fun toRedeliveryMappingView(item: RedeliveryMapping): RedeliveryMappingView {
    return RedeliveryMappingView(
        code = item.code,
        name = item.name,
        checksumText = item.checksum,
        notes = item.notes,
        maximum = "%.2f".format(item.maximum),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryMappingViews(items: MutableList<RedeliveryMapping>): MutableList<RedeliveryMappingView> {
    val views = mutableListOf<RedeliveryMappingView>()
    for (item in items) {
        views.add(toRedeliveryMappingView(item))
    }
    return views
}

fun sumRedeliveryMappingView(view: RedeliveryMappingView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
