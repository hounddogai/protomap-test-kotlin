package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit quota. */
data class AuditQuotaView(
    var codeText: String,
    var labelText: String,
    var odometerText: String,
    var threshold: String,
    var graceDays: String,
    var rateText: String,
    var pallets: String,
    var title: String,
    var costText: String,
    var channel: String,
    var statusLabel: String,
)

fun toAuditQuotaView(item: AuditQuota): AuditQuotaView {
    return AuditQuotaView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        odometerText = item.odometer.toString(),
        threshold = "%.2f".format(item.threshold),
        graceDays = item.graceDays.toString(),
        rateText = "%.2f".format(item.rate),
        pallets = item.pallets.toString(),
        title = item.title,
        costText = "%.2f".format(item.cost),
        channel = truncate(item.channel, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditQuotaViews(items: MutableList<AuditQuota>): MutableList<AuditQuotaView> {
    val views = mutableListOf<AuditQuotaView>()
    for (item in items) {
        views.add(toAuditQuotaView(item))
    }
    return views
}

fun sumAuditQuotaView(view: AuditQuotaView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedOdometer.toDouble() + parsedThreshold + parsedGraceDays.toDouble()
}
