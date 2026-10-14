package com.acme.transport.customs.declaration.milestone

/** How screens and exports show a adaptive declaration milestone. */
data class AdaptiveDeclarationMilestoneView(
    var code: String,
    var labelText: String,
    var laneText: String,
    var priority: String,
    var archived: String,
    var discount: String,
    var statusLabel: String,
)

fun toAdaptiveDeclarationMilestoneView(item: AdaptiveDeclarationMilestone): AdaptiveDeclarationMilestoneView {
    return AdaptiveDeclarationMilestoneView(
        code = item.code,
        labelText = item.label,
        laneText = item.lane,
        priority = item.priority.toString(),
        archived = "${item.archived}",
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDeclarationMilestoneViews(items: MutableList<AdaptiveDeclarationMilestone>): MutableList<AdaptiveDeclarationMilestoneView> {
    val views = mutableListOf<AdaptiveDeclarationMilestoneView>()
    for (item in items) {
        views.add(toAdaptiveDeclarationMilestoneView(item))
    }
    return views
}

fun sumAdaptiveDeclarationMilestoneView(view: AdaptiveDeclarationMilestoneView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedPriority.toDouble() + parsedDiscount
}
