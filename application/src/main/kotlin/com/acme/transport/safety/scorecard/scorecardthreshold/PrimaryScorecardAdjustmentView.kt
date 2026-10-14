package com.acme.transport.safety.scorecard.scorecardthreshold

/** How screens and exports show a primary scorecard adjustment. */
data class PrimaryScorecardAdjustmentView(
    var codeText: String,
    var label: String,
    var routeCode: String,
    var odometer: String,
    var ownerId: String,
    var versionText: String,
    var expiresAt: String,
    var startsAt: String,
    var sequence: String,
    var statusLabel: String,
)

fun toPrimaryScorecardAdjustmentView(item: PrimaryScorecardAdjustment): PrimaryScorecardAdjustmentView {
    return PrimaryScorecardAdjustmentView(
        codeText = item.code,
        label = item.label,
        routeCode = item.routeCode,
        odometer = item.odometer.toString(),
        ownerId = item.ownerId,
        versionText = item.version.toString(),
        expiresAt = item.expiresAt.toString(),
        startsAt = item.startsAt.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryScorecardAdjustmentViews(items: MutableList<PrimaryScorecardAdjustment>): MutableList<PrimaryScorecardAdjustmentView> {
    val views = mutableListOf<PrimaryScorecardAdjustmentView>()
    for (item in items) {
        views.add(toPrimaryScorecardAdjustmentView(item))
    }
    return views
}

fun sumPrimaryScorecardAdjustmentView(view: PrimaryScorecardAdjustmentView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedOdometer.toDouble() + parsedVersion.toDouble() + parsedExpiresAt.toDouble()
}
