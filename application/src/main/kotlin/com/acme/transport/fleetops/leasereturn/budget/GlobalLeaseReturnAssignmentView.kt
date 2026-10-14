package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global lease return assignment. */
data class GlobalLeaseReturnAssignmentView(
    var code: String,
    var titleText: String,
    var durationMinutesText: String,
    var carrier: String,
    var ratio: String,
    var startsAt: String,
    var channel: String,
    var limitText: String,
    var maximumText: String,
    var discount: String,
    var depotText: String,
    var statusLabel: String,
)

fun toGlobalLeaseReturnAssignmentView(item: GlobalLeaseReturnAssignment): GlobalLeaseReturnAssignmentView {
    return GlobalLeaseReturnAssignmentView(
        code = truncate(item.code, 80),
        titleText = truncate(item.title, 64),
        durationMinutesText = item.durationMinutes.toString(),
        carrier = truncate(item.carrier, 80),
        ratio = "%.2f".format(item.ratio),
        startsAt = item.startsAt.toString(),
        channel = truncate(item.channel, 64),
        limitText = item.limit.toString(),
        maximumText = "%.2f".format(item.maximum),
        discount = "%.2f".format(item.discount),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalLeaseReturnAssignmentViews(items: MutableList<GlobalLeaseReturnAssignment>): MutableList<GlobalLeaseReturnAssignmentView> {
    val views = mutableListOf<GlobalLeaseReturnAssignmentView>()
    for (item in items) {
        views.add(toGlobalLeaseReturnAssignmentView(item))
    }
    return views
}

fun sumGlobalLeaseReturnAssignmentView(view: GlobalLeaseReturnAssignmentView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedRatio + parsedStartsAt.toDouble()
}
