package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability group. */
data class AvailabilityGroupView(
    var codeText: String,
    var nameText: String,
    var budget: String,
    var phone: String,
    var description: String,
    var statusLabel: String,
)

fun toAvailabilityGroupView(item: AvailabilityGroup): AvailabilityGroupView {
    return AvailabilityGroupView(
        codeText = item.code,
        nameText = truncate(item.name, 120),
        budget = "%.2f".format(item.budget),
        phone = item.phone,
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityGroupViews(items: MutableList<AvailabilityGroup>): MutableList<AvailabilityGroupView> {
    val views = mutableListOf<AvailabilityGroupView>()
    for (item in items) {
        views.add(toAvailabilityGroupView(item))
    }
    return views
}

fun sumAvailabilityGroupView(view: AvailabilityGroupView): Double {
    val parsedBudget = view.budget.toDouble()
    return parsedBudget
}
