package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock quota. */
data class CrossDockQuotaView(
    var codeText: String,
    var nameText: String,
    var active: String,
    var amount: String,
    var version: String,
    var minimum: String,
    var segment: String,
    var weight: String,
    var statusLabel: String,
)

fun toCrossDockQuotaView(item: CrossDockQuota): CrossDockQuotaView {
    return CrossDockQuotaView(
        codeText = item.code,
        nameText = item.name,
        active = "${item.active}",
        amount = "%.2f".format(item.amount),
        version = item.version.toString(),
        minimum = "%.2f".format(item.minimum),
        segment = truncate(item.segment, 40),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockQuotaViews(items: MutableList<CrossDockQuota>): MutableList<CrossDockQuotaView> {
    val views = mutableListOf<CrossDockQuotaView>()
    for (item in items) {
        views.add(toCrossDockQuotaView(item))
    }
    return views
}

fun sumCrossDockQuotaView(view: CrossDockQuotaView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedAmount + parsedVersion.toDouble() + parsedMinimum
}
