package com.acme.transport.fleetops.serviceplan.agreement

/** How screens and exports show a manual service plan segment. */
data class ManualServicePlanSegmentView(
    var codeText: String,
    var title: String,
    var minimumText: String,
    var batchSizeText: String,
    var cost: String,
    var weightText: String,
    var locale: String,
    var rate: String,
    var driverId: String,
    var statusLabel: String,
)

fun toManualServicePlanSegmentView(item: ManualServicePlanSegment): ManualServicePlanSegmentView {
    return ManualServicePlanSegmentView(
        codeText = item.code,
        title = item.title,
        minimumText = "%.2f".format(item.minimum),
        batchSizeText = item.batchSize.toString(),
        cost = "%.2f".format(item.cost),
        weightText = "%.2f".format(item.weight),
        locale = item.locale,
        rate = "%.2f".format(item.rate),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualServicePlanSegmentViews(items: MutableList<ManualServicePlanSegment>): MutableList<ManualServicePlanSegmentView> {
    val views = mutableListOf<ManualServicePlanSegmentView>()
    for (item in items) {
        views.add(toManualServicePlanSegmentView(item))
    }
    return views
}

fun sumManualServicePlanSegmentView(view: ManualServicePlanSegmentView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedMinimum + parsedBatchSize.toDouble() + parsedCost
}
