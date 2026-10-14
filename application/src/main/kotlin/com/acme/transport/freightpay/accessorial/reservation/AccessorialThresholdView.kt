package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial threshold. */
data class AccessorialThresholdView(
    var code: String,
    var name: String,
    var contactEmail: String,
    var routeCode: String,
    var expiresAt: String,
    var labelText: String,
    var costText: String,
    var minimum: String,
    var archivedText: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toAccessorialThresholdView(item: AccessorialThreshold): AccessorialThresholdView {
    return AccessorialThresholdView(
        code = item.code,
        name = item.name,
        contactEmail = item.contactEmail,
        routeCode = item.routeCode,
        expiresAt = item.expiresAt.toString(),
        labelText = truncate(item.label, 64),
        costText = "%.2f".format(item.cost),
        minimum = "%.2f".format(item.minimum),
        archivedText = "${item.archived}",
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialThresholdViews(items: MutableList<AccessorialThreshold>): MutableList<AccessorialThresholdView> {
    val views = mutableListOf<AccessorialThresholdView>()
    for (item in items) {
        views.add(toAccessorialThresholdView(item))
    }
    return views
}

fun sumAccessorialThresholdView(view: AccessorialThresholdView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedExpiresAt.toDouble() + parsedCost + parsedMinimum
}
