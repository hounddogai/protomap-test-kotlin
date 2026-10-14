package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute rule. */
data class DisputeRuleView(
    var code: String,
    var title: String,
    var durationMinutes: String,
    var source: String,
    var threshold: String,
    var checksumText: String,
    var driverIdText: String,
    var ratio: String,
    var region: String,
    var statusLabel: String,
)

fun toDisputeRuleView(item: DisputeRule): DisputeRuleView {
    return DisputeRuleView(
        code = truncate(item.code, 40),
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        source = item.source,
        threshold = "%.2f".format(item.threshold),
        checksumText = truncate(item.checksum, 64),
        driverIdText = item.driverId,
        ratio = "%.2f".format(item.ratio),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeRuleViews(items: MutableList<DisputeRule>): MutableList<DisputeRuleView> {
    val views = mutableListOf<DisputeRuleView>()
    for (item in items) {
        views.add(toDisputeRuleView(item))
    }
    return views
}

fun sumDisputeRuleView(view: DisputeRuleView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedDurationMinutes.toDouble() + parsedThreshold + parsedRatio
}
