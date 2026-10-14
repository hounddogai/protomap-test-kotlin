package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel audit. */
data class ParcelAuditView(
    var code: String,
    var titleText: String,
    var archived: String,
    var channel: String,
    var active: String,
    var driverIdText: String,
    var loadKg: String,
    var dock: String,
    var unit: String,
    var createdAt: String,
    var weight: String,
    var statusLabel: String,
)

fun toParcelAuditView(item: ParcelAudit): ParcelAuditView {
    return ParcelAuditView(
        code = item.code,
        titleText = item.title,
        archived = "${item.archived}",
        channel = truncate(item.channel, 64),
        active = "${item.active}",
        driverIdText = item.driverId,
        loadKg = "%.2f".format(item.loadKg),
        dock = item.dock,
        unit = truncate(item.unit, 80),
        createdAt = item.createdAt.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelAuditViews(items: MutableList<ParcelAudit>): MutableList<ParcelAuditView> {
    val views = mutableListOf<ParcelAuditView>()
    for (item in items) {
        views.add(toParcelAuditView(item))
    }
    return views
}

fun sumParcelAuditView(view: ParcelAuditView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedLoadKg + parsedCreatedAt.toDouble() + parsedWeight
}
