package com.acme.transport.network.servicearea.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient service area audit. */
data class LenientServiceAreaAuditView(
    var code: String,
    var title: String,
    var visibleText: String,
    var depot: String,
    var stopCount: String,
    var channel: String,
    var cost: String,
    var statusLabel: String,
)

fun toLenientServiceAreaAuditView(item: LenientServiceAreaAudit): LenientServiceAreaAuditView {
    return LenientServiceAreaAuditView(
        code = truncate(item.code, 120),
        title = item.title,
        visibleText = "${item.visible}",
        depot = item.depot,
        stopCount = item.stopCount.toString(),
        channel = truncate(item.channel, 64),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientServiceAreaAuditViews(items: MutableList<LenientServiceAreaAudit>): MutableList<LenientServiceAreaAuditView> {
    val views = mutableListOf<LenientServiceAreaAuditView>()
    for (item in items) {
        views.add(toLenientServiceAreaAuditView(item))
    }
    return views
}

fun sumLenientServiceAreaAuditView(view: LenientServiceAreaAuditView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedStopCount.toDouble() + parsedCost
}
