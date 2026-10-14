package com.acme.transport.safety.violation.proposal

/** How screens and exports show a default violation segment. */
data class DefaultViolationSegmentView(
    var code: String,
    var nameText: String,
    var title: String,
    var currency: String,
    var capacity: String,
    var statusLabel: String,
)

fun toDefaultViolationSegmentView(item: DefaultViolationSegment): DefaultViolationSegmentView {
    return DefaultViolationSegmentView(
        code = item.code,
        nameText = item.name,
        title = item.title,
        currency = item.currency,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultViolationSegmentViews(items: MutableList<DefaultViolationSegment>): MutableList<DefaultViolationSegmentView> {
    val views = mutableListOf<DefaultViolationSegmentView>()
    for (item in items) {
        views.add(toDefaultViolationSegmentView(item))
    }
    return views
}

fun sumDefaultViolationSegmentView(view: DefaultViolationSegmentView): Double {
    val parsedCapacity = view.capacity.toInt()
    return parsedCapacity.toDouble()
}
