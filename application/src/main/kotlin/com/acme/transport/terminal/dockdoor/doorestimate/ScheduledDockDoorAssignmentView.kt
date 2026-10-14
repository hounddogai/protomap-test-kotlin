package com.acme.transport.terminal.dockdoor.doorestimate

/** How screens and exports show a scheduled dock door assignment. */
data class ScheduledDockDoorAssignmentView(
    var code: String,
    var label: String,
    var shiftCode: String,
    var pallets: String,
    var capacity: String,
    var quantity: String,
    var statusLabel: String,
)

fun toScheduledDockDoorAssignmentView(item: ScheduledDockDoorAssignment): ScheduledDockDoorAssignmentView {
    return ScheduledDockDoorAssignmentView(
        code = item.code,
        label = item.label,
        shiftCode = item.shiftCode,
        pallets = item.pallets.toString(),
        capacity = item.capacity.toString(),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDockDoorAssignmentViews(items: MutableList<ScheduledDockDoorAssignment>): MutableList<ScheduledDockDoorAssignmentView> {
    val views = mutableListOf<ScheduledDockDoorAssignmentView>()
    for (item in items) {
        views.add(toScheduledDockDoorAssignmentView(item))
    }
    return views
}

fun sumScheduledDockDoorAssignmentView(view: ScheduledDockDoorAssignmentView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedPallets.toDouble() + parsedCapacity.toDouble() + parsedQuantity.toDouble()
}
