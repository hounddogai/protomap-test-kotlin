package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity plan. */
data class CapacityPlanView(
    var code: String,
    var label: String,
    var ceiling: String,
    var pinnedText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toCapacityPlanView(item: CapacityPlan): CapacityPlanView {
    return CapacityPlanView(
        code = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        pinnedText = "${item.pinned}",
        ownerId = truncate(item.ownerId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityPlanViews(items: MutableList<CapacityPlan>): MutableList<CapacityPlanView> {
    val views = mutableListOf<CapacityPlanView>()
    for (item in items) {
        views.add(toCapacityPlanView(item))
    }
    return views
}

fun sumCapacityPlanView(view: CapacityPlanView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCeiling
}
