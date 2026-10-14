package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit exception. */
data class AuditExceptionView(
    var code: String,
    var name: String,
    var fuelLitersText: String,
    var contactEmail: String,
    var title: String,
    var createdAtText: String,
    var cost: String,
    var durationMinutes: String,
    var vehicleId: String,
    var expiresAt: String,
    var maximum: String,
    var statusLabel: String,
)

fun toAuditExceptionView(item: AuditException): AuditExceptionView {
    return AuditExceptionView(
        code = item.code,
        name = truncate(item.name, 64),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        contactEmail = item.contactEmail,
        title = item.title,
        createdAtText = item.createdAt.toString(),
        cost = "%.2f".format(item.cost),
        durationMinutes = item.durationMinutes.toString(),
        vehicleId = truncate(item.vehicleId, 80),
        expiresAt = item.expiresAt.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditExceptionViews(items: MutableList<AuditException>): MutableList<AuditExceptionView> {
    val views = mutableListOf<AuditExceptionView>()
    for (item in items) {
        views.add(toAuditExceptionView(item))
    }
    return views
}

fun sumAuditExceptionView(view: AuditExceptionView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedFuelLiters + parsedCreatedAt.toDouble() + parsedCost
}
