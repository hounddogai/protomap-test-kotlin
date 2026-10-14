package com.acme.transport.freightpay.audit.budget

/** How screens and exports show a audit profile. */
data class AuditProfileView(
    var code: String,
    var name: String,
    var version: String,
    var expiresAt: String,
    var startsAt: String,
    var floorValue: String,
    var depot: String,
    var statusLabel: String,
)

fun toAuditProfileView(item: AuditProfile): AuditProfileView {
    return AuditProfileView(
        code = item.code,
        name = item.name,
        version = item.version.toString(),
        expiresAt = item.expiresAt.toString(),
        startsAt = item.startsAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditProfileViews(items: MutableList<AuditProfile>): MutableList<AuditProfileView> {
    val views = mutableListOf<AuditProfileView>()
    for (item in items) {
        views.add(toAuditProfileView(item))
    }
    return views
}

fun sumAuditProfileView(view: AuditProfileView): Double {
    val parsedVersion = view.version.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedVersion.toDouble() + parsedExpiresAt.toDouble() + parsedStartsAt.toDouble()
}
