package com.acme.transport.crewing.swap.swapschedule

/** How screens and exports show a adaptive swap schedule. */
data class AdaptiveSwapScheduleView(
    var code: String,
    var label: String,
    var sequence: String,
    var capacity: String,
    var approved: String,
    var cost: String,
    var contactEmail: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toAdaptiveSwapScheduleView(item: AdaptiveSwapSchedule): AdaptiveSwapScheduleView {
    return AdaptiveSwapScheduleView(
        code = item.code,
        label = item.label,
        sequence = item.sequence.toString(),
        capacity = item.capacity.toString(),
        approved = "${item.approved}",
        cost = "%.2f".format(item.cost),
        contactEmail = item.contactEmail,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveSwapScheduleViews(items: MutableList<AdaptiveSwapSchedule>): MutableList<AdaptiveSwapScheduleView> {
    val views = mutableListOf<AdaptiveSwapScheduleView>()
    for (item in items) {
        views.add(toAdaptiveSwapScheduleView(item))
    }
    return views
}

fun sumAdaptiveSwapScheduleView(view: AdaptiveSwapScheduleView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedSequence.toDouble() + parsedCapacity.toDouble() + parsedCost
}
