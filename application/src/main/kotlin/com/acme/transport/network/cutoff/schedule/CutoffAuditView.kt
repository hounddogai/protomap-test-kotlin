package com.acme.transport.network.cutoff.schedule

/** How screens and exports show a cutoff audit. */
data class CutoffAuditView(
    var codeText: String,
    var title: String,
    var distanceKm: String,
    var threshold: String,
    var ratio: String,
    var version: String,
    var depot: String,
    var startsAt: String,
    var rate: String,
    var pinned: String,
    var statusLabel: String,
)

fun toCutoffAuditView(item: CutoffAudit): CutoffAuditView {
    return CutoffAuditView(
        codeText = item.code,
        title = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        threshold = "%.2f".format(item.threshold),
        ratio = "%.2f".format(item.ratio),
        version = item.version.toString(),
        depot = item.depot,
        startsAt = item.startsAt.toString(),
        rate = "%.2f".format(item.rate),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffAuditViews(items: MutableList<CutoffAudit>): MutableList<CutoffAuditView> {
    val views = mutableListOf<CutoffAuditView>()
    for (item in items) {
        views.add(toCutoffAuditView(item))
    }
    return views
}

fun sumCutoffAuditView(view: CutoffAuditView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedDistanceKm + parsedThreshold + parsedRatio
}
