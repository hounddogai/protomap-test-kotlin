package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted inspection allocation. */
data class WeightedInspectionAllocationView(
    var code: String,
    var name: String,
    var floorValue: String,
    var description: String,
    var weight: String,
    var approved: String,
    var source: String,
    var checksum: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toWeightedInspectionAllocationView(item: WeightedInspectionAllocation): WeightedInspectionAllocationView {
    return WeightedInspectionAllocationView(
        code = item.code,
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        description = item.description,
        weight = "%.2f".format(item.weight),
        approved = "${item.approved}",
        source = truncate(item.source, 64),
        checksum = item.checksum,
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedInspectionAllocationViews(items: MutableList<WeightedInspectionAllocation>): MutableList<WeightedInspectionAllocationView> {
    val views = mutableListOf<WeightedInspectionAllocationView>()
    for (item in items) {
        views.add(toWeightedInspectionAllocationView(item))
    }
    return views
}

fun sumWeightedInspectionAllocationView(view: WeightedInspectionAllocationView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedFloorValue + parsedWeight + parsedBatchSize.toDouble()
}
