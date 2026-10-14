package com.acme.transport.terminal.crossdock.schedule

/** How screens and exports show a tiered cross dock rule. */
data class TieredCrossDockRuleView(
    var code: String,
    var labelText: String,
    var archivedText: String,
    var expiresAt: String,
    var batchSize: String,
    var thresholdText: String,
    var timezoneText: String,
    var visible: String,
    var approved: String,
    var statusLabel: String,
)

fun toTieredCrossDockRuleView(item: TieredCrossDockRule): TieredCrossDockRuleView {
    return TieredCrossDockRuleView(
        code = item.code,
        labelText = item.label,
        archivedText = "${item.archived}",
        expiresAt = item.expiresAt.toString(),
        batchSize = item.batchSize.toString(),
        thresholdText = "%.2f".format(item.threshold),
        timezoneText = item.timezone,
        visible = "${item.visible}",
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCrossDockRuleViews(items: MutableList<TieredCrossDockRule>): MutableList<TieredCrossDockRuleView> {
    val views = mutableListOf<TieredCrossDockRuleView>()
    for (item in items) {
        views.add(toTieredCrossDockRuleView(item))
    }
    return views
}

fun sumTieredCrossDockRuleView(view: TieredCrossDockRuleView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedExpiresAt.toDouble() + parsedBatchSize.toDouble() + parsedThreshold
}
