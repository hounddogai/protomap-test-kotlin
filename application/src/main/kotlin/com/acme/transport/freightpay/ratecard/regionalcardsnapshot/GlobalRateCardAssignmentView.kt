package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

/** How screens and exports show a global rate card assignment. */
data class GlobalRateCardAssignmentView(
    var code: String,
    var name: String,
    var title: String,
    var ratioText: String,
    var capacity: String,
    var externalRef: String,
    var durationMinutes: String,
    var sequenceText: String,
    var archived: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toGlobalRateCardAssignmentView(item: GlobalRateCardAssignment): GlobalRateCardAssignmentView {
    return GlobalRateCardAssignmentView(
        code = item.code,
        name = item.name,
        title = item.title,
        ratioText = "%.2f".format(item.ratio),
        capacity = item.capacity.toString(),
        externalRef = item.externalRef,
        durationMinutes = item.durationMinutes.toString(),
        sequenceText = item.sequence.toString(),
        archived = "${item.archived}",
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRateCardAssignmentViews(items: MutableList<GlobalRateCardAssignment>): MutableList<GlobalRateCardAssignmentView> {
    val views = mutableListOf<GlobalRateCardAssignmentView>()
    for (item in items) {
        views.add(toGlobalRateCardAssignmentView(item))
    }
    return views
}

fun sumGlobalRateCardAssignmentView(view: GlobalRateCardAssignmentView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedRatio + parsedCapacity.toDouble() + parsedDurationMinutes.toDouble()
}
