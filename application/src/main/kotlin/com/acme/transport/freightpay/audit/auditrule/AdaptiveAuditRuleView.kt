package com.acme.transport.freightpay.audit.auditrule

/** How screens and exports show a adaptive audit rule. */
data class AdaptiveAuditRuleView(
    var code: String,
    var nameText: String,
    var weightText: String,
    var batchSizeText: String,
    var channel: String,
    var minimum: String,
    var rate: String,
    var windowDays: String,
    var loadKg: String,
    var stopCountText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toAdaptiveAuditRuleView(item: AdaptiveAuditRule): AdaptiveAuditRuleView {
    return AdaptiveAuditRuleView(
        code = item.code,
        nameText = item.name,
        weightText = "%.2f".format(item.weight),
        batchSizeText = item.batchSize.toString(),
        channel = item.channel,
        minimum = "%.2f".format(item.minimum),
        rate = "%.2f".format(item.rate),
        windowDays = item.windowDays.toString(),
        loadKg = "%.2f".format(item.loadKg),
        stopCountText = item.stopCount.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAuditRuleViews(items: MutableList<AdaptiveAuditRule>): MutableList<AdaptiveAuditRuleView> {
    val views = mutableListOf<AdaptiveAuditRuleView>()
    for (item in items) {
        views.add(toAdaptiveAuditRuleView(item))
    }
    return views
}

fun sumAdaptiveAuditRuleView(view: AdaptiveAuditRuleView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedWeight + parsedBatchSize.toDouble() + parsedMinimum
}
