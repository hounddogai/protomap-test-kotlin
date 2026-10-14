package com.acme.transport.customs.inspection.inspectiondigest

/** How screens and exports show a adaptive inspection allocation. */
data class AdaptiveInspectionAllocationView(
    var code: String,
    var name: String,
    var quantity: String,
    var etaMinutesText: String,
    var sequence: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionAllocationView(item: AdaptiveInspectionAllocation): AdaptiveInspectionAllocationView {
    return AdaptiveInspectionAllocationView(
        code = item.code,
        name = item.name,
        quantity = item.quantity.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        sequence = item.sequence.toString(),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionAllocationViews(items: MutableList<AdaptiveInspectionAllocation>): MutableList<AdaptiveInspectionAllocationView> {
    val views = mutableListOf<AdaptiveInspectionAllocationView>()
    for (item in items) {
        views.add(toAdaptiveInspectionAllocationView(item))
    }
    return views
}

fun sumAdaptiveInspectionAllocationView(view: AdaptiveInspectionAllocationView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedQuantity.toDouble() + parsedEtaMinutes.toDouble() + parsedSequence.toDouble()
}
