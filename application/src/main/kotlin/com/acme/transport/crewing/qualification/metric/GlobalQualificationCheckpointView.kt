package com.acme.transport.crewing.qualification.metric

/** How screens and exports show a global qualification checkpoint. */
data class GlobalQualificationCheckpointView(
    var code: String,
    var name: String,
    var source: String,
    var summary: String,
    var channel: String,
    var visibleText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toGlobalQualificationCheckpointView(item: GlobalQualificationCheckpoint): GlobalQualificationCheckpointView {
    return GlobalQualificationCheckpointView(
        code = item.code,
        name = item.name,
        source = item.source,
        summary = item.summary,
        channel = item.channel,
        visibleText = "${item.visible}",
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalQualificationCheckpointViews(items: MutableList<GlobalQualificationCheckpoint>): MutableList<GlobalQualificationCheckpointView> {
    val views = mutableListOf<GlobalQualificationCheckpointView>()
    for (item in items) {
        views.add(toGlobalQualificationCheckpointView(item))
    }
    return views
}

fun sumGlobalQualificationCheckpointView(view: GlobalQualificationCheckpointView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
