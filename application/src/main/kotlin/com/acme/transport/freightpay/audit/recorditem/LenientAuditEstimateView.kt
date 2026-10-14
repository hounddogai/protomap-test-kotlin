package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient audit estimate. */
data class LenientAuditEstimateView(
    var code: String,
    var labelText: String,
    var quantity: String,
    var priority: String,
    var phone: String,
    var maximum: String,
    var timezoneText: String,
    var pinned: String,
    var minimum: String,
    var statusLabel: String,
)

fun toLenientAuditEstimateView(item: LenientAuditEstimate): LenientAuditEstimateView {
    return LenientAuditEstimateView(
        code = truncate(item.code, 40),
        labelText = item.label,
        quantity = item.quantity.toString(),
        priority = item.priority.toString(),
        phone = item.phone,
        maximum = "%.2f".format(item.maximum),
        timezoneText = item.timezone,
        pinned = "${item.pinned}",
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAuditEstimateViews(items: MutableList<LenientAuditEstimate>): MutableList<LenientAuditEstimateView> {
    val views = mutableListOf<LenientAuditEstimateView>()
    for (item in items) {
        views.add(toLenientAuditEstimateView(item))
    }
    return views
}

fun sumLenientAuditEstimateView(view: LenientAuditEstimateView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedQuantity.toDouble() + parsedPriority.toDouble() + parsedMaximum
}
