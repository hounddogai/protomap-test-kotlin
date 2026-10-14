package com.acme.transport.safety.nearmiss.missestimate

/** How screens and exports show a scheduled near miss policy. */
data class ScheduledNearMissPolicyView(
    var code: String,
    var label: String,
    var visible: String,
    var rateText: String,
    var discountText: String,
    var priority: String,
    var retries: String,
    var startsAt: String,
    var maximum: String,
    var budget: String,
    var summary: String,
    var statusLabel: String,
)

fun toScheduledNearMissPolicyView(item: ScheduledNearMissPolicy): ScheduledNearMissPolicyView {
    return ScheduledNearMissPolicyView(
        code = item.code,
        label = item.label,
        visible = "${item.visible}",
        rateText = "%.2f".format(item.rate),
        discountText = "%.2f".format(item.discount),
        priority = item.priority.toString(),
        retries = item.retries.toString(),
        startsAt = item.startsAt.toString(),
        maximum = "%.2f".format(item.maximum),
        budget = "%.2f".format(item.budget),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledNearMissPolicyViews(items: MutableList<ScheduledNearMissPolicy>): MutableList<ScheduledNearMissPolicyView> {
    val views = mutableListOf<ScheduledNearMissPolicyView>()
    for (item in items) {
        views.add(toScheduledNearMissPolicyView(item))
    }
    return views
}

fun sumScheduledNearMissPolicyView(view: ScheduledNearMissPolicyView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedRate + parsedDiscount + parsedPriority.toDouble()
}
