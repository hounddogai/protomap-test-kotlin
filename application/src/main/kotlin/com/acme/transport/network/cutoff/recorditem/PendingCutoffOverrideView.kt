package com.acme.transport.network.cutoff.recorditem

/** How screens and exports show a pending cutoff override. */
data class PendingCutoffOverrideView(
    var codeText: String,
    var title: String,
    var segment: String,
    var stopCount: String,
    var distanceKm: String,
    var versionText: String,
    var amount: String,
    var pallets: String,
    var trailerId: String,
    var startsAt: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toPendingCutoffOverrideView(item: PendingCutoffOverride): PendingCutoffOverrideView {
    return PendingCutoffOverrideView(
        codeText = item.code,
        title = item.title,
        segment = item.segment,
        stopCount = item.stopCount.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        versionText = item.version.toString(),
        amount = "%.2f".format(item.amount),
        pallets = item.pallets.toString(),
        trailerId = item.trailerId,
        startsAt = item.startsAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCutoffOverrideViews(items: MutableList<PendingCutoffOverride>): MutableList<PendingCutoffOverrideView> {
    val views = mutableListOf<PendingCutoffOverrideView>()
    for (item in items) {
        views.add(toPendingCutoffOverrideView(item))
    }
    return views
}

fun sumPendingCutoffOverrideView(view: PendingCutoffOverrideView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedStopCount.toDouble() + parsedDistanceKm + parsedVersion.toDouble()
}
