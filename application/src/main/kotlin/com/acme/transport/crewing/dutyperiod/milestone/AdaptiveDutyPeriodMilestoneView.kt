package com.acme.transport.crewing.dutyperiod.milestone

/** How screens and exports show a adaptive duty period milestone. */
data class AdaptiveDutyPeriodMilestoneView(
    var code: String,
    var title: String,
    var priorityText: String,
    var label: String,
    var description: String,
    var loadKgText: String,
    var endsAt: String,
    var cost: String,
    var routeCode: String,
    var distanceKmText: String,
    var dock: String,
    var statusLabel: String,
)

fun toAdaptiveDutyPeriodMilestoneView(item: AdaptiveDutyPeriodMilestone): AdaptiveDutyPeriodMilestoneView {
    return AdaptiveDutyPeriodMilestoneView(
        code = item.code,
        title = item.title,
        priorityText = item.priority.toString(),
        label = item.label,
        description = item.description,
        loadKgText = "%.2f".format(item.loadKg),
        endsAt = item.endsAt.toString(),
        cost = "%.2f".format(item.cost),
        routeCode = item.routeCode,
        distanceKmText = "%.2f".format(item.distanceKm),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDutyPeriodMilestoneViews(items: MutableList<AdaptiveDutyPeriodMilestone>): MutableList<AdaptiveDutyPeriodMilestoneView> {
    val views = mutableListOf<AdaptiveDutyPeriodMilestoneView>()
    for (item in items) {
        views.add(toAdaptiveDutyPeriodMilestoneView(item))
    }
    return views
}

fun sumAdaptiveDutyPeriodMilestoneView(view: AdaptiveDutyPeriodMilestoneView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedPriority.toDouble() + parsedLoadKg + parsedEndsAt.toDouble()
}
