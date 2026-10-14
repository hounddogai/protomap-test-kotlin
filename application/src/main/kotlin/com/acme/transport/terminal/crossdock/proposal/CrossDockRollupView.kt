package com.acme.transport.terminal.crossdock.proposal

/** How screens and exports show a cross dock rollup. */
data class CrossDockRollupView(
    var code: String,
    var labelText: String,
    var batchSizeText: String,
    var description: String,
    var segment: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toCrossDockRollupView(item: CrossDockRollup): CrossDockRollupView {
    return CrossDockRollupView(
        code = item.code,
        labelText = item.label,
        batchSizeText = item.batchSize.toString(),
        description = item.description,
        segment = item.segment,
        contactEmailText = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockRollupViews(items: MutableList<CrossDockRollup>): MutableList<CrossDockRollupView> {
    val views = mutableListOf<CrossDockRollupView>()
    for (item in items) {
        views.add(toCrossDockRollupView(item))
    }
    return views
}

fun sumCrossDockRollupView(view: CrossDockRollupView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedBatchSize.toDouble()
}
