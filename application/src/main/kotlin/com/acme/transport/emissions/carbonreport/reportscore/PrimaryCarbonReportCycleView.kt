package com.acme.transport.emissions.carbonreport.reportscore

/** How screens and exports show a primary carbon report cycle. */
data class PrimaryCarbonReportCycleView(
    var code: String,
    var nameText: String,
    var etaMinutes: String,
    var revision: String,
    var checksum: String,
    var visibleText: String,
    var ownerId: String,
    var cost: String,
    var limit: String,
    var routeCode: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toPrimaryCarbonReportCycleView(item: PrimaryCarbonReportCycle): PrimaryCarbonReportCycleView {
    return PrimaryCarbonReportCycleView(
        code = item.code,
        nameText = item.name,
        etaMinutes = item.etaMinutes.toString(),
        revision = item.revision.toString(),
        checksum = item.checksum,
        visibleText = "${item.visible}",
        ownerId = item.ownerId,
        cost = "%.2f".format(item.cost),
        limit = item.limit.toString(),
        routeCode = item.routeCode,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCarbonReportCycleViews(items: MutableList<PrimaryCarbonReportCycle>): MutableList<PrimaryCarbonReportCycleView> {
    val views = mutableListOf<PrimaryCarbonReportCycleView>()
    for (item in items) {
        views.add(toPrimaryCarbonReportCycleView(item))
    }
    return views
}

fun sumPrimaryCarbonReportCycleView(view: PrimaryCarbonReportCycleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRevision.toDouble() + parsedCost
}
