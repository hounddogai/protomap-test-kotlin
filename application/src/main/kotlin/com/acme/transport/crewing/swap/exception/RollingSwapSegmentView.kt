package com.acme.transport.crewing.swap.exception

/** How screens and exports show a rolling swap segment. */
data class RollingSwapSegmentView(
    var code: String,
    var name: String,
    var discount: String,
    var description: String,
    var maximum: String,
    var priority: String,
    var capacityText: String,
    var pinned: String,
    var driverId: String,
    var attempts: String,
    var segment: String,
    var statusLabel: String,
)

fun toRollingSwapSegmentView(item: RollingSwapSegment): RollingSwapSegmentView {
    return RollingSwapSegmentView(
        code = item.code,
        name = item.name,
        discount = "%.2f".format(item.discount),
        description = item.description,
        maximum = "%.2f".format(item.maximum),
        priority = item.priority.toString(),
        capacityText = item.capacity.toString(),
        pinned = "${item.pinned}",
        driverId = item.driverId,
        attempts = item.attempts.toString(),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingSwapSegmentViews(items: MutableList<RollingSwapSegment>): MutableList<RollingSwapSegmentView> {
    val views = mutableListOf<RollingSwapSegmentView>()
    for (item in items) {
        views.add(toRollingSwapSegmentView(item))
    }
    return views
}

fun sumRollingSwapSegmentView(view: RollingSwapSegmentView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedDiscount + parsedMaximum + parsedPriority.toDouble()
}
