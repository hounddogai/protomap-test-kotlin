package com.acme.transport.network.servicearea.areabatch

/** How screens and exports show a regional service area batch. */
data class RegionalServiceAreaBatchView(
    var code: String,
    var name: String,
    var batchSize: String,
    var revisionText: String,
    var phoneText: String,
    var floorValueText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toRegionalServiceAreaBatchView(item: RegionalServiceAreaBatch): RegionalServiceAreaBatchView {
    return RegionalServiceAreaBatchView(
        code = item.code,
        name = item.name,
        batchSize = item.batchSize.toString(),
        revisionText = item.revision.toString(),
        phoneText = item.phone,
        floorValueText = "%.2f".format(item.floorValue),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalServiceAreaBatchViews(items: MutableList<RegionalServiceAreaBatch>): MutableList<RegionalServiceAreaBatchView> {
    val views = mutableListOf<RegionalServiceAreaBatchView>()
    for (item in items) {
        views.add(toRegionalServiceAreaBatchView(item))
    }
    return views
}

fun sumRegionalServiceAreaBatchView(view: RegionalServiceAreaBatchView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedBatchSize.toDouble() + parsedRevision.toDouble() + parsedFloorValue
}
