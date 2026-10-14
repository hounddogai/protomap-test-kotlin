package com.acme.transport.fleetops.defect.forecast

/** How screens and exports show a fallback defect rule. */
data class FallbackDefectRuleView(
    var code: String,
    var title: String,
    var createdAt: String,
    var reviewerText: String,
    var capacity: String,
    var minimum: String,
    var statusLabel: String,
)

fun toFallbackDefectRuleView(item: FallbackDefectRule): FallbackDefectRuleView {
    return FallbackDefectRuleView(
        code = item.code,
        title = item.title,
        createdAt = item.createdAt.toString(),
        reviewerText = item.reviewer,
        capacity = item.capacity.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDefectRuleViews(items: MutableList<FallbackDefectRule>): MutableList<FallbackDefectRuleView> {
    val views = mutableListOf<FallbackDefectRuleView>()
    for (item in items) {
        views.add(toFallbackDefectRuleView(item))
    }
    return views
}

fun sumFallbackDefectRuleView(view: FallbackDefectRuleView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedCreatedAt.toDouble() + parsedCapacity.toDouble() + parsedMinimum
}
