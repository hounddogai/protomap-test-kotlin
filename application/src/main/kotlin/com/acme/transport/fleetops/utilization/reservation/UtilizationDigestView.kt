package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization digest. */
data class UtilizationDigestView(
    var code: String,
    var title: String,
    var localeText: String,
    var region: String,
    var sourceText: String,
    var trailerId: String,
    var summary: String,
    var amount: String,
    var statusLabel: String,
)

fun toUtilizationDigestView(item: UtilizationDigest): UtilizationDigestView {
    return UtilizationDigestView(
        code = item.code,
        title = truncate(item.title, 120),
        localeText = truncate(item.locale, 120),
        region = truncate(item.region, 120),
        sourceText = item.source,
        trailerId = item.trailerId,
        summary = item.summary,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationDigestViews(items: MutableList<UtilizationDigest>): MutableList<UtilizationDigestView> {
    val views = mutableListOf<UtilizationDigestView>()
    for (item in items) {
        views.add(toUtilizationDigestView(item))
    }
    return views
}

fun sumUtilizationDigestView(view: UtilizationDigestView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
