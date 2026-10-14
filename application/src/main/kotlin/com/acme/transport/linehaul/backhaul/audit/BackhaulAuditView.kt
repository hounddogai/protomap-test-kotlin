package com.acme.transport.linehaul.backhaul.audit

/** How screens and exports show a backhaul audit. */
data class BackhaulAuditView(
    var code: String,
    var label: String,
    var channel: String,
    var margin: String,
    var expiresAtText: String,
    var distanceKm: String,
    var fuelLiters: String,
    var sequence: String,
    var statusLabel: String,
)

fun toBackhaulAuditView(item: BackhaulAudit): BackhaulAuditView {
    return BackhaulAuditView(
        code = item.code,
        label = item.label,
        channel = item.channel,
        margin = "%.2f".format(item.margin),
        expiresAtText = item.expiresAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        fuelLiters = "%.2f".format(item.fuelLiters),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulAuditViews(items: MutableList<BackhaulAudit>): MutableList<BackhaulAuditView> {
    val views = mutableListOf<BackhaulAuditView>()
    for (item in items) {
        views.add(toBackhaulAuditView(item))
    }
    return views
}

fun sumBackhaulAuditView(view: BackhaulAuditView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedMargin + parsedExpiresAt.toDouble() + parsedDistanceKm
}
