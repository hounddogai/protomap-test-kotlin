package com.acme.transport.emissions.emissionfactor.factorpreference

/** How screens and exports show a adaptive emission factor assignment. */
data class AdaptiveEmissionFactorAssignmentView(
    var code: String,
    var label: String,
    var source: String,
    var graceDays: String,
    var priorityText: String,
    var ratio: String,
    var active: String,
    var statusLabel: String,
)

fun toAdaptiveEmissionFactorAssignmentView(item: AdaptiveEmissionFactorAssignment): AdaptiveEmissionFactorAssignmentView {
    return AdaptiveEmissionFactorAssignmentView(
        code = item.code,
        label = item.label,
        source = item.source,
        graceDays = item.graceDays.toString(),
        priorityText = item.priority.toString(),
        ratio = "%.2f".format(item.ratio),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveEmissionFactorAssignmentViews(items: MutableList<AdaptiveEmissionFactorAssignment>): MutableList<AdaptiveEmissionFactorAssignmentView> {
    val views = mutableListOf<AdaptiveEmissionFactorAssignmentView>()
    for (item in items) {
        views.add(toAdaptiveEmissionFactorAssignmentView(item))
    }
    return views
}

fun sumAdaptiveEmissionFactorAssignmentView(view: AdaptiveEmissionFactorAssignmentView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedGraceDays.toDouble() + parsedPriority.toDouble() + parsedRatio
}
