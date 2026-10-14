package com.acme.transport.customs.inspection.batch

/** How screens and exports show a inspection request. */
data class InspectionRequestView(
    var code: String,
    var titleText: String,
    var carrier: String,
    var priority: String,
    var description: String,
    var statusLabel: String,
)

fun toInspectionRequestView(item: InspectionRequest): InspectionRequestView {
    return InspectionRequestView(
        code = item.code,
        titleText = item.title,
        carrier = item.carrier,
        priority = item.priority.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionRequestViews(items: MutableList<InspectionRequest>): MutableList<InspectionRequestView> {
    val views = mutableListOf<InspectionRequestView>()
    for (item in items) {
        views.add(toInspectionRequestView(item))
    }
    return views
}

fun sumInspectionRequestView(view: InspectionRequestView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
