package com.acme.transport.lastmile.redelivery.ledger

/** How screens and exports show a lenient redelivery audit. */
data class LenientRedeliveryAuditView(
    var code: String,
    var label: String,
    var distanceKm: String,
    var odometer: String,
    var approved: String,
    var minimumText: String,
    var ceiling: String,
    var marginText: String,
    var statusLabel: String,
)

fun toLenientRedeliveryAuditView(item: LenientRedeliveryAudit): LenientRedeliveryAuditView {
    return LenientRedeliveryAuditView(
        code = item.code,
        label = item.label,
        distanceKm = "%.2f".format(item.distanceKm),
        odometer = item.odometer.toString(),
        approved = "${item.approved}",
        minimumText = "%.2f".format(item.minimum),
        ceiling = "%.2f".format(item.ceiling),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRedeliveryAuditViews(items: MutableList<LenientRedeliveryAudit>): MutableList<LenientRedeliveryAuditView> {
    val views = mutableListOf<LenientRedeliveryAuditView>()
    for (item in items) {
        views.add(toLenientRedeliveryAuditView(item))
    }
    return views
}

fun sumLenientRedeliveryAuditView(view: LenientRedeliveryAuditView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedDistanceKm + parsedOdometer.toDouble() + parsedMinimum
}
