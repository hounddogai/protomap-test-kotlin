package com.acme.transport.freightpay.dispute.disputeassignment

/** How screens and exports show a capped dispute audit. */
data class CappedDisputeAuditView(
    var code: String,
    var labelText: String,
    var marginText: String,
    var version: String,
    var distanceKmText: String,
    var shiftCode: String,
    var stopCount: String,
    var odometer: String,
    var phoneText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toCappedDisputeAuditView(item: CappedDisputeAudit): CappedDisputeAuditView {
    return CappedDisputeAuditView(
        code = item.code,
        labelText = item.label,
        marginText = "%.2f".format(item.margin),
        version = item.version.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        shiftCode = item.shiftCode,
        stopCount = item.stopCount.toString(),
        odometer = item.odometer.toString(),
        phoneText = item.phone,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDisputeAuditViews(items: MutableList<CappedDisputeAudit>): MutableList<CappedDisputeAuditView> {
    val views = mutableListOf<CappedDisputeAuditView>()
    for (item in items) {
        views.add(toCappedDisputeAuditView(item))
    }
    return views
}

fun sumCappedDisputeAuditView(view: CappedDisputeAuditView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedMargin + parsedVersion.toDouble() + parsedDistanceKm
}
