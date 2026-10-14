package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity group. */
data class CapacityGroupView(
    var code: String,
    var label: String,
    var ratioText: String,
    var startsAt: String,
    var approved: String,
    var budget: String,
    var statusLabel: String,
)

fun toCapacityGroupView(item: CapacityGroup): CapacityGroupView {
    return CapacityGroupView(
        code = truncate(item.code, 80),
        label = item.label,
        ratioText = "%.2f".format(item.ratio),
        startsAt = item.startsAt.toString(),
        approved = "${item.approved}",
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityGroupViews(items: MutableList<CapacityGroup>): MutableList<CapacityGroupView> {
    val views = mutableListOf<CapacityGroupView>()
    for (item in items) {
        views.add(toCapacityGroupView(item))
    }
    return views
}

fun sumCapacityGroupView(view: CapacityGroupView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedRatio + parsedStartsAt.toDouble() + parsedBudget
}
