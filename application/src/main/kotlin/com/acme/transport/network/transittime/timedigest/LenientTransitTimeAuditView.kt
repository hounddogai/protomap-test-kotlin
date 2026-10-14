package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient transit time audit. */
data class LenientTransitTimeAuditView(
    var code: String,
    var titleText: String,
    var active: String,
    var driverId: String,
    var category: String,
    var quantity: String,
    var statusLabel: String,
)

fun toLenientTransitTimeAuditView(item: LenientTransitTimeAudit): LenientTransitTimeAuditView {
    return LenientTransitTimeAuditView(
        code = item.code,
        titleText = truncate(item.title, 80),
        active = "${item.active}",
        driverId = item.driverId,
        category = truncate(item.category, 120),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTransitTimeAuditViews(items: MutableList<LenientTransitTimeAudit>): MutableList<LenientTransitTimeAuditView> {
    val views = mutableListOf<LenientTransitTimeAuditView>()
    for (item in items) {
        views.add(toLenientTransitTimeAuditView(item))
    }
    return views
}

fun sumLenientTransitTimeAuditView(view: LenientTransitTimeAuditView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
