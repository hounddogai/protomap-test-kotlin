package com.acme.transport.fleetops.serviceplan.recorditem

/** How screens and exports show a strict service plan assignment. */
data class StrictServicePlanAssignmentView(
    var codeText: String,
    var title: String,
    var archived: String,
    var sequenceText: String,
    var quantity: String,
    var thresholdText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toStrictServicePlanAssignmentView(item: StrictServicePlanAssignment): StrictServicePlanAssignmentView {
    return StrictServicePlanAssignmentView(
        codeText = item.code,
        title = item.title,
        archived = "${item.archived}",
        sequenceText = item.sequence.toString(),
        quantity = item.quantity.toString(),
        thresholdText = "%.2f".format(item.threshold),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictServicePlanAssignmentViews(items: MutableList<StrictServicePlanAssignment>): MutableList<StrictServicePlanAssignmentView> {
    val views = mutableListOf<StrictServicePlanAssignmentView>()
    for (item in items) {
        views.add(toStrictServicePlanAssignmentView(item))
    }
    return views
}

fun sumStrictServicePlanAssignmentView(view: StrictServicePlanAssignmentView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedSequence.toDouble() + parsedQuantity.toDouble() + parsedThreshold
}
