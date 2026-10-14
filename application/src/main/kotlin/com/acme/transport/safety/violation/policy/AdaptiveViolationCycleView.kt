package com.acme.transport.safety.violation.policy

/** How screens and exports show a adaptive violation cycle. */
data class AdaptiveViolationCycleView(
    var code: String,
    var title: String,
    var version: String,
    var capacity: String,
    var budget: String,
    var attempts: String,
    var statusLabel: String,
)

fun toAdaptiveViolationCycleView(item: AdaptiveViolationCycle): AdaptiveViolationCycleView {
    return AdaptiveViolationCycleView(
        code = item.code,
        title = item.title,
        version = item.version.toString(),
        capacity = item.capacity.toString(),
        budget = "%.2f".format(item.budget),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveViolationCycleViews(items: MutableList<AdaptiveViolationCycle>): MutableList<AdaptiveViolationCycleView> {
    val views = mutableListOf<AdaptiveViolationCycleView>()
    for (item in items) {
        views.add(toAdaptiveViolationCycleView(item))
    }
    return views
}

fun sumAdaptiveViolationCycleView(view: AdaptiveViolationCycleView): Double {
    val parsedVersion = view.version.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedVersion.toDouble() + parsedCapacity.toDouble() + parsedBudget
}
