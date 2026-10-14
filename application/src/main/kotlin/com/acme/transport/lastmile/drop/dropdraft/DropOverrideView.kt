package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop override. */
data class DropOverrideView(
    var code: String,
    var labelText: String,
    var checksum: String,
    var active: String,
    var carrier: String,
    var weight: String,
    var depot: String,
    var budget: String,
    var description: String,
    var phoneText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toDropOverrideView(item: DropOverride): DropOverrideView {
    return DropOverrideView(
        code = item.code,
        labelText = truncate(item.label, 40),
        checksum = item.checksum,
        active = "${item.active}",
        carrier = truncate(item.carrier, 40),
        weight = "%.2f".format(item.weight),
        depot = truncate(item.depot, 40),
        budget = "%.2f".format(item.budget),
        description = item.description,
        phoneText = item.phone,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropOverrideViews(items: MutableList<DropOverride>): MutableList<DropOverrideView> {
    val views = mutableListOf<DropOverrideView>()
    for (item in items) {
        views.add(toDropOverrideView(item))
    }
    return views
}

fun sumDropOverrideView(view: DropOverrideView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedWeight + parsedBudget + parsedCapacity.toDouble()
}
